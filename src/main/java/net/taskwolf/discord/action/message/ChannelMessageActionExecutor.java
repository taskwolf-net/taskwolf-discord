package net.taskwolf.discord.action.message;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final String guildIdentifier;
  private final String channelIdentifier;
  private String message;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    if (jda.getGuildById(guildIdentifier) == null) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    message = placeholderDissolve.dissolve(message);
    var channel = jda.getTextChannelById(channelIdentifier);
    if (channel == null) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.channel.not.found");
    }
    if (!message.isEmpty()) {
      channel.sendMessage(message).queue();
    }
    return ActionResult.futureSuccess(buildInformation(channel));
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("message", message);
    information.put("channelName", channel.getName());
    return information;
  }
}
