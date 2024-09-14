package com.dulno.discord.select;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.user.User;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentSelectEntry;
import com.dulno.discord.guild.GuildDatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class GuildSelect implements InputComponentSelect {
  private final GuildDatabaseTable guildDatabaseTable;
  private final JDA jda;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return guildDatabaseTable.findGuilds(target)
      .thenApply(guilds -> guilds.stream()
        .map(guild -> InputComponentSelectEntry.create(guild,
          jda.getGuildById(guild).getName()))
        .collect(Collectors.toList()));
  }
}