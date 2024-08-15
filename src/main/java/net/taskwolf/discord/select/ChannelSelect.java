package net.taskwolf.discord.select;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.user.User;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
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
  public CompletableFuture<List<String>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return CompletableFuture.completedFuture(
      jda.getGuildById(previousInputs.get("guildIdentifier")).getTextChannels()
        .stream().map(channel -> new JSONObject(Map.of("identifier", channel.getId(),
          "name", channel.getName())).toString()).collect(Collectors.toList()));
  }
}