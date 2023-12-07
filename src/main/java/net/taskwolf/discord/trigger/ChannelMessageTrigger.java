package net.taskwolf.discord.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ChannelMessageTrigger implements Trigger {
  public static TriggerInformation information(
    InputComponentSelect guildComponentSelect,
    InputComponentSelect channelComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("Receive new channel message")
      .withDescription("Receive new channel message")
      .withIdentifier("discord-channel-message-trigger")
      .withInputVariable(InputComponentVariable.createSelect("Server",
        "guildIdentifier", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("Channel",
        "channelIdentifier", channelComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("Message", "message"))
      .withOutputVariable(OutputComponentVariable.create("Message Author Identifier",
        "messageAuthorIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("Message Author Name",
        "messageAuthorName")).build();
  }

  public static ChannelMessageTrigger of(JSONObject content) {
    return create(content.getString("guildIdentifier"),
      content.getString("channelIdentifier"));
  }

  private final String guildIdentifier;
  private final String channelIdentifier;
}
