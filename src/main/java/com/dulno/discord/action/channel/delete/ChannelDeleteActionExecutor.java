package com.dulno.discord.action.channel.delete;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelDeleteActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final String guildIdentifier;
  private String channelIdentifier;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.channel.delete.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    channelIdentifier = placeholderDissolve.dissolve(channelIdentifier);
    var channel = jda.getTextChannelById(channelIdentifier);
    if (channel == null) {
      return ActionResult.futureFailure("discord.action.channel.delete.failure.channel.not.found");
    }
    channel.delete().queue();
    return ActionResult.futureSuccess(buildInformation());
  }

  private Map<String, Object> buildInformation() {
    var information = Maps.<String, Object>newHashMap();
    information.put("channelIdentifier", channelIdentifier);
    return information;
  }
}