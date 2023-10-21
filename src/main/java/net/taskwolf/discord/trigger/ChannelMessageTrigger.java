package net.taskwolf.discord.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.ComponentType;
import net.taskwolf.core.workflow.component.ComponentVariable;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ChannelMessageTrigger implements Trigger {
  public static TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("Receive new channel message")
      .withDescription("Receive new channel message")
      .withIdentifier("discord-channel-message-trigger")
      .withInputVariable(ComponentVariable.create("Guild Identifier",
        "guildIdentifier", ComponentType.TEXT))
      .withInputVariable(ComponentVariable.create("Channel Identifier",
        "channelIdentifier", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message",
        "message", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message Author Identifier",
        "messageAuthorIdentifier", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message Author Name",
        "messageAuthorName", ComponentType.TEXT)).build();
  }

  public static ChannelMessageTrigger of(JSONObject content) {
    return create(content.getString("guildIdentifier"),
      content.getString("channelIdentifier"));
  }

  private final String guildIdentifier;
  private final String channelIdentifier;
}
