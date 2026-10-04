package net.taskwolf.discord.action.channel.delete;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import net.taskwolf.discord.guild.GuildDatabaseTable;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.workflow.action.ActionExecutor;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.workflow.placeholder.PlaceholderDissolve;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelDeleteActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final UUID ownerId;
  private final String guildIdentifier;
  private String channelIdentifier;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var placeholderDissolve = PlaceholderDissolve.create(information);
    channelIdentifier = placeholderDissolve.dissolve(channelIdentifier);
    return guildDatabaseTable.userGuildsExists(ownerId)
      .thenCompose(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(boolean hasGuilds) {
    if (!hasGuilds) {
      return ActionResult.futureFailure("discord.action.channel.delete.failure.server.not.found");
    }
    return guildDatabaseTable.findGuilds(ownerId)
      .thenComposeAsync(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(List<String> guilds) {
    if (!guilds.contains(guildIdentifier)) {
      return ActionResult.futureFailure("discord.action.channel.delete.failure.server.not.found");
    }
    return deleteChannel();
  }

  public CompletableFuture<ActionResult> deleteChannel() {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.channel.delete.failure.server.not.found");
    }
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