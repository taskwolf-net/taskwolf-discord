package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import org.json.JSONObject;

import java.util.Map;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action {
  public static ActionInformation information(
    InputComponentSelect guildComponentSelect, InputComponentSelect channelComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("Send channel message")
      .withDescription("Send channel message")
      .withIdentifier("discord-channel-message-action")
      .withInputVariable(InputComponentVariable.createSelect("Server",
        "guildIdentifier", "The server on which the message is to be sent", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("Channel",
        "channelIdentifier", "The channel in which the message is to be sent", channelComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("Message",
        "message", "The message to be sent", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("Message", "message"))
      .withOutputVariable(OutputComponentVariable.create("Channel Name",
        "channelName")).build();
  }

  public static ChannelMessageAction of(JDA jda, JSONObject content) {
    return create(jda, content.getString("guildIdentifier"),
      content.getString("channelIdentifier"), content.getString("message"));
  }

  private final JDA jda;
  private final String guildIdentifier;
  private final String channelIdentifier;
  private String message;

  @Override
  public ActionResult execute(Map<String, Object> information) {
    if (jda.getGuildById(guildIdentifier) == null) {
      return ActionResult.failure("An error occurred while sending a discord " +
        "channel message. The error occurred because the selected server " +
        "could not be found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    message = placeholderDissolve.dissolve(message);
    var channel = jda.getTextChannelById(channelIdentifier);
    if (channel == null) {
      return ActionResult.failure("An error occurred while sending a " +
        "discord channel message. The error occurred because the selected " +
        "channel could not be found");
    }
    channel.sendMessage(message).queue();
    return ActionResult.success(buildInformation(channel));
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("message", message);
    information.put("channelName", channel.getName());
    return information;
  }
}
