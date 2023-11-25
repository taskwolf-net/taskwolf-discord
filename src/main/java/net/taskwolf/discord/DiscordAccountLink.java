package net.taskwolf.discord;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.discord.access.GuildDatabaseTable;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public final class DiscordAccountLink implements AccountLink {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;

  @Override
  public CompletableFuture<Boolean> accountExists(UUID userId) {
    var futureResponse = new CompletableFuture<Boolean>();
    guildDatabaseTable.userGuildsExists(userId).thenApply(exists ->
      checkGuildExistence(userId, exists).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  @Override
  public CompletableFuture<List<String>> findAccounts(UUID userId) {
    return guildDatabaseTable.findGuilds(userId).thenApply(guilds ->
      guilds.stream().map(guild -> new JSONObject(Map.of("identifier", guild, "name",
        jda.getGuildById(guild).getName())).toString()).collect(Collectors.toList()));
  }

  @Override
  public void removeAccount(UUID userId, String identifier) {
    guildDatabaseTable.removeGuild(userId, identifier);
  }

  private CompletableFuture<Boolean> checkGuildExistence(
    UUID userId, boolean guildsExists
  ) {
    if (!guildsExists) {
      return CompletableFuture.completedFuture(false);
    }
    var futureResponse = new CompletableFuture<Boolean>();
    guildDatabaseTable.findGuilds(userId).thenAccept(guilds ->
      futureResponse.complete(removeInactiveGuilds(userId, guilds)));
    return futureResponse;
  }

  private boolean removeInactiveGuilds(UUID userId, List<String> guilds) {
    var botGuilds = jda.getGuilds();
    var removedGuilds = Lists.<String>newArrayList();
    for (var guild : guilds) {
      if (botGuilds.stream().noneMatch(botBuild -> botBuild.getId().equals(guild))) {
        removedGuilds.add(guild);
      }
    }
    guilds.removeAll(removedGuilds);
    if (!removedGuilds.isEmpty()) {
      guildDatabaseTable.updateUserGuilds(userId, guilds);
    }
    return !guilds.isEmpty();
  }

  private static final String DISCORD_REGISTRATION_URL = "https://discord.com/api/oauth2/authorize?client_id=1160570527224701048&permissions=8&redirect_uri=https%3A%2F%2Fapi.taskwolf.net%2Fdiscord%2Fguild%2Fadd%2F&state=API-KEY&response_type=code&scope=guilds%20bot%20identify";

  @Override
  public String registrationUrl(String apiKey) {
    return DISCORD_REGISTRATION_URL.replace("API-KEY", apiKey);
  }

  @Override
  public String description() {
    return "Would you like to add another Discord server to integrate it into your automation with the help of Taskwolf? Just click on the logo.";
  }
}
