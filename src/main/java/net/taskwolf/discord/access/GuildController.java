package net.taskwolf.discord.access;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.user.UserDatabaseTable;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.List;
import java.util.UUID;

@RestController
public class GuildController extends TaskwolfRestController {
  private final GuildDatabaseTable guildDatabaseTable;

  private GuildController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    GuildDatabaseTable guildDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.guildDatabaseTable = guildDatabaseTable;
  }

  @RequestMapping(path = "/discord/guild/add/", method = RequestMethod.GET)
  public void addGuild(
    HttpServletRequest request, HttpServletResponse response, @RequestParam("state") String apiKey,
    @RequestParam("guild_id") String guildId
  ) {
    if (!isValidApiKey(apiKey)) {
      return;
    }
    var userId = findUserId(apiKey);
    guildDatabaseTable.userGuildsExists(userId).thenAccept(exists ->
      addGuild(userId, guildId, exists));
    response.setHeader("Location", "https://google.com");
    response.setStatus(302);
  }

  private void addGuild(UUID userId, String guildId, boolean userGuildsExists) {
    if (!userGuildsExists) {
      guildDatabaseTable.addGuild(userId, guildId);
      return;
    }
    guildDatabaseTable.findGuilds(userId).thenAccept(guilds ->
      addGuild(userId, guildId, guilds));
  }

  private void addGuild(UUID userId, String guildId, List<String> guilds) {
    if (guilds.contains(guildId)) {
      return;
    }
    guildDatabaseTable.addGuild(userId, guildId);
  }
}
