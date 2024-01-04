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
      .withName("discord.trigger.channel.message.name")
      .withDescription("discord.trigger.channel.message.description")
      .withIdentifier("discord-channel-message-trigger")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.channel.message.input.server.name",
        "guildIdentifier", "discord.trigger.channel.message.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.channel.message.input.channel.name",
        "channelIdentifier", "discord.trigger.channel.message.input.channel.description", channelComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message",
        "message"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message.author.identifier",
        "messageAuthorIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message.author.name",
        "messageAuthorName")).build();
  }

  public static ChannelMessageTrigger of(JSONObject content) {
    return create(content.getString("guildIdentifier"),
      content.getString("channelIdentifier"));
  }

  private final String guildIdentifier;
  private final String channelIdentifier;
}
