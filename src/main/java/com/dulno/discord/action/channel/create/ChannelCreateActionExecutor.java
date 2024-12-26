package com.dulno.discord.action.channel.create;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelCreateActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final String guildIdentifier;
  private String name;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.channel.create.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    name = placeholderDissolve.dissolve(name);
    if (name.isEmpty()) {
      return ActionResult.futureFailure("discord.action.channel.create.failure.name.empty");
    }
    var futureResponse = new CompletableFuture<ActionResult>();
    guild.createTextChannel(name).queue(channel ->
      futureResponse.complete(ActionResult.success(buildInformation(channel))));
    return futureResponse;
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("channelName", channel.getName());
    information.put("channelIdentifier", channel.getId());
    return information;
  }
}
