package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.workflow.component.ComponentSelect;
import net.taskwolf.core.workflow.component.ComponentType;
import net.taskwolf.core.workflow.component.ComponentVariable;
import org.json.JSONObject;

import java.util.Map;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action {
  public static ActionInformation information(
    ComponentSelect guildComponentSelect, ComponentSelect channelComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("Send channel message")
      .withDescription("Send channel message")
      .withIdentifier("discord-channel-message-action")
      .withInputVariable(ComponentVariable.createSelect("Server",
        "guildIdentifier", guildComponentSelect))
      .withInputVariable(ComponentVariable.createSelect("Channel",
        "channelIdentifier", channelComponentSelect))
      .withInputVariable(ComponentVariable.create("Message",
        "message", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Channel Name",
        "channelName", ComponentType.TEXT)).build();
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
  public Map<String, Object> execute(Map<String, Object> information) {
    for (var entry : information.entrySet()) {
      message = message.replace("$" + entry.getKey(), entry.getValue().toString());
    }
    var channel = jda.getChannelById(TextChannel.class, channelIdentifier);
    channel.sendMessage(message);
    return buildInformation(channel);
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("guildIdentifier", guildIdentifier);
    information.put("channelIdentifier", channelIdentifier);
    information.put("channelName", channel.getName());
    information.put("message", message);
    return information;
  }
}
