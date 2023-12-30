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
    HttpServletRequest request, HttpServletResponse response,
    @RequestParam("state") String state, @RequestParam("guild_id") String guildId
  ) throws Exception {
    response.setStatus(310);
    var apiKey = state.substring(0, 232);
    if (!isValidApiKey(apiKey)) {
      return;
    }
    var id = UUID.fromString(state.substring(232));
    guildDatabaseTable.userGuildsExists(id).thenAccept(exists ->
      addGuild(id, guildId, exists));
  }

  private void addGuild(UUID id, String guildId, boolean userGuildsExists) {
    if (!userGuildsExists) {
      guildDatabaseTable.addGuild(id, guildId);
      return;
    }
    guildDatabaseTable.findGuilds(id).thenAccept(guilds ->
      addGuild(id, guildId, guilds));
  }

  private void addGuild(UUID id, String guildId, List<String> guilds) {
    if (guilds.contains(guildId)) {
      return;
    }
    guildDatabaseTable.addGuild(id, guildId);
  }
}
