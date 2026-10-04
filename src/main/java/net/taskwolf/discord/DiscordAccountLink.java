package net.taskwolf.discord;

import net.taskwolf.core.account.AccountLinkEntry;
import net.taskwolf.core.environment.TaskwolfEnvironment;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.discord.guild.GuildDatabaseTable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DiscordAccountLink implements AccountLink {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final DiscordConfiguration configuration;
  private final TaskwolfEnvironment environment;

  @Override
  public CompletableFuture<Boolean> accountExists(UUID id) {
    var futureResponse = new CompletableFuture<Boolean>();
    guildDatabaseTable.userGuildsExists(id).thenApply(exists ->
      checkGuildExistence(id, exists).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  @Override
  public CompletableFuture<List<AccountLinkEntry>> findAccounts(UUID id) {
    return guildDatabaseTable.findGuildsIfExists(id)
      .thenApply(guilds -> guilds.stream()
        .map(guild -> AccountLinkEntry.create(guild,
          jda.getGuildById(guild).getName()))
        .toList());
  }

  @Override
  public void removeAccount(UUID id, String identifier) {
    guildDatabaseTable.guildOccurNumber(identifier).thenAccept(number ->
      removeAccount(id, identifier, number));
  }

  private void removeAccount(
    UUID id, String guildIdentifier, int guildOccurNumber
  ) {
    if (guildOccurNumber == 1) {
      jda.getGuildById(guildIdentifier).leave().queue();
    }
    guildDatabaseTable.removeGuild(id, guildIdentifier);
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

  private boolean removeInactiveGuilds(UUID id, List<String> guilds) {
    var botGuilds = jda.getGuilds();
    var removedGuilds = Lists.<String>newArrayList();
    for (var guild : guilds) {
      if (botGuilds.stream().noneMatch(botGuild -> botGuild.getId().equals(guild))) {
        removedGuilds.add(guild);
      }
    }
    guilds.removeAll(removedGuilds);
    if (!removedGuilds.isEmpty()) {
      guildDatabaseTable.updateUserGuilds(id, guilds);
    }
    return !guilds.isEmpty();
  }

  private static final String DISCORD_REGISTRATION_URL = "https://discord.com/api/oauth2/authorize?client_id=CLIENT-ID&permissions=8&redirect_uri=https%3A%2F%2FDOMAIN%2Fv1%2Fdiscord%2Fguild%2Fadd%2F&state=TASKWOLF-STATE&response_type=code&scope=guilds%20bot%20identify";

  @Override
  public String registrationUrl(UUID id, String apiKey) {
    return DISCORD_REGISTRATION_URL
      .replace("CLIENT-ID", configuration.clientId())
      .replace("TASKWOLF-STATE", apiKey + "TASKWOLF-STATE-SPLIT" + id.toString())
      .replace("DOMAIN", environment.publicEndpoint());
  }

  @Override
  public String description() {
    return "discord.account.link.description";
  }
}
