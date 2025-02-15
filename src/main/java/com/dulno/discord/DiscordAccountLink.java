package com.dulno.discord;

import com.dulno.core.account.AccountLinkEntry;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.account.AccountLink;
import com.dulno.discord.guild.GuildDatabaseTable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DiscordAccountLink implements AccountLink {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;

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

  private static final String DISCORD_REGISTRATION_URL = "https://discord.com/api/oauth2/authorize?client_id=1160570527224701048&permissions=8&redirect_uri=https%3A%2F%2Fapi.dulno.com%2Fv1%2Fdiscord%2Fguild%2Fadd%2F&state=DULNO-STATE&response_type=code&scope=guilds%20bot%20identify";

  @Override
  public String registrationUrl(UUID id, String apiKey) {
    return DISCORD_REGISTRATION_URL.replace("DULNO-STATE", apiKey + "DULNO-STATE-SPLIT" + id.toString());
  }

  @Override
  public String description() {
    return "discord.account.link.description";
  }
}
