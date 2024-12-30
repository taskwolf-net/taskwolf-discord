package com.dulno.discord.select;

import com.dulno.discord.guild.GuildDatabaseTable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.user.User;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentSelectEntry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class ChannelSelect implements InputComponentSelect {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    try {
      var guildId = previousInputs.get("guildIdentifier");
      return guildDatabaseTable.userGuildsExists(target)
        .thenCompose(exists -> checkGuildExistence(guildId, target, exists));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
  }

  private CompletableFuture<List<InputComponentSelectEntry>> checkGuildExistence(
    String guildId, UUID target, boolean guildExists
  ) {
    if (!guildExists) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return guildDatabaseTable.findGuilds(target)
      .thenApplyAsync(guilds -> checkGuildAccess(guildId, guilds));
  }

  private List<InputComponentSelectEntry> checkGuildAccess(
    String guildId, List<String> guilds
  ) {
    if (!guilds.contains(guildId)) {
      return Lists.newArrayList();
    }
    return jda.getGuildById(guildId).getTextChannels().stream()
      .map(channel -> InputComponentSelectEntry.create(
        channel.getId(), channel.getName()))
      .toList();
  }
}