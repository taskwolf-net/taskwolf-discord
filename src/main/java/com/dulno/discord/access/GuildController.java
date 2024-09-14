package com.dulno.discord.access;

import com.dulno.core.access.DulnoRestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.discord.guild.GuildDatabaseTable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public class GuildController extends DulnoRestController {
  private final GuildDatabaseTable guildDatabaseTable;
  private final TeamTargetDatabaseTable teamTargetDatabaseTable;

  private GuildController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    GuildDatabaseTable guildDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.guildDatabaseTable = guildDatabaseTable;
    this.teamTargetDatabaseTable = teamTargetDatabaseTable;
  }

  @RequestMapping(path = "/discord/guild/add/", method = RequestMethod.GET)
  public void addGuild(
    HttpServletRequest request, HttpServletResponse response,
    @RequestParam("state") String state, @RequestParam("guild_id") String guildId
  ) throws Exception {
    response.sendRedirect("https://dulno.com/close/");
    var split = state.split("DULNO-STATE-SPLIT");
    var apiKey = split[0];
    if (!isValidApiKey(apiKey)) {
      return;
    }
    var id = UUID.fromString(split[1]);
    userDatabaseTable().findUser(findUserId(apiKey))
      .thenAccept(user -> checkUserAuthorization(user, id)
        .thenAccept(isAuthorized -> guildDatabaseTable.userGuildsExists(id)
          .thenAccept(exists -> addGuild(id, guildId, isAuthorized, exists))));
  }

  private void addGuild(
    UUID id, String guildId, boolean isAuthorized, boolean userGuildsExists
  ) {
    if (!isAuthorized) {
      return;
    }
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

  private CompletableFuture<Boolean> checkUserAuthorization(User user, UUID id) {
    if (id.equals(user.id()) || user.organizations().contains(id)) {
      return CompletableFuture.completedFuture(true);
    }
    return teamTargetDatabaseTable.findTargetSecured(user.id()).thenApply(
      teamTarget -> teamTarget.map(uuid -> uuid.equals(id)).orElse(false));
  }
}
