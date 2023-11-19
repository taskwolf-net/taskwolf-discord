package net.taskwolf.discord.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.ComponentSelect;
import net.taskwolf.core.workflow.component.ComponentType;
import net.taskwolf.core.workflow.component.ComponentVariable;
import net.taskwolf.discord.access.GuildDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ChannelMessageTrigger implements Trigger {
  public static TriggerInformation information(
    JDA jda, GuildDatabaseTable guildDatabaseTable
  ) {
    return TriggerInformation.builder()
      .withName("Receive new channel message")
      .withDescription("Receive new channel message")
      .withIdentifier("discord-channel-message-trigger")
      .withInputVariable(ComponentVariable.createSelect("Server",
        "guildIdentifier", createGuildComponentSelect(jda, guildDatabaseTable)))
      .withInputVariable(ComponentVariable.createSelect("Channel",
        "channelIdentifier", createChannelComponentSelect(jda)))
      .withOutputVariable(ComponentVariable.create("Message",
        "message", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message Author Identifier",
        "messageAuthorIdentifier", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message Author Name",
        "messageAuthorName", ComponentType.TEXT)).build();
  }

  private static ComponentSelect createGuildComponentSelect(
    JDA jda, GuildDatabaseTable guildDatabaseTable
  ) {
    return (user, previousInputs) -> guildDatabaseTable.findGuilds(user)
      .thenApply(guilds -> guilds.stream().map(guild ->
        new JSONObject(Map.of("identifier", guild, "name",
          jda.getGuildById(guild).getName())).toString()).collect(Collectors.toList()));
  }

  private static ComponentSelect createChannelComponentSelect(JDA jda) {
    return (user, previousInputs) -> CompletableFuture.completedFuture(
      jda.getGuildById(previousInputs.get("guildIdentifier")).getTextChannels()
        .stream().map(channel -> new JSONObject(Map.of("identifier", channel.getId(),
          "name", channel.getName())).toString()).collect(Collectors.toList()));
  }

  public static ChannelMessageTrigger of(JSONObject content) {
    return create(content.getString("guildIdentifier"),
      content.getString("channelIdentifier"));
  }

  private final String guildIdentifier;
  private final String channelIdentifier;
}
