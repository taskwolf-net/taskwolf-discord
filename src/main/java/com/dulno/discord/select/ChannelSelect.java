package com.dulno.discord.select;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.user.User;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentSelectEntry;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class ChannelSelect implements InputComponentSelect {
  private final JDA jda;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return CompletableFuture.completedFuture(
      jda.getGuildById(previousInputs.get("guildIdentifier")).getTextChannels()
        .stream().map(channel -> InputComponentSelectEntry.create(channel.getId(),
          channel.getName())).collect(Collectors.toList()));
  }
}