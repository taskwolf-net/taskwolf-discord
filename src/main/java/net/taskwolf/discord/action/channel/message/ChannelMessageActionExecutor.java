package net.taskwolf.discord.action.channel.message;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import net.taskwolf.discord.guild.GuildDatabaseTable;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.workflow.action.ActionExecutor;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.workflow.placeholder.PlaceholderDissolve;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final UUID ownerId;
  private final String guildIdentifier;
  private final String channelIdentifier;
  private String message;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var placeholderDissolve = PlaceholderDissolve.create(information);
    message = placeholderDissolve.dissolve(message);
    return guildDatabaseTable.userGuildsExists(ownerId)
      .thenCompose(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(boolean hasGuilds) {
    if (!hasGuilds) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.server.not.found");
    }
    return guildDatabaseTable.findGuilds(ownerId)
      .thenComposeAsync(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(List<String> guilds) {
    if (!guilds.contains(guildIdentifier)) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.server.not.found");
    }
    return sendChannelMessage();
  }

  public CompletableFuture<ActionResult> sendChannelMessage() {
    if (jda.getGuildById(guildIdentifier) == null) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.server.not.found");
    }
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
