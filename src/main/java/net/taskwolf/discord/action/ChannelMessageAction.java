package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.core.action.Action;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action {
  public static ChannelMessageAction of(JDA jda, String content) {
    return create(jda, "", "");
  }

  private final JDA jda;
  private final String channelIdentifier;
  private final String message;

  @Override
  public Map<String, Object> execute(Map<String, Object> information) {
    for (var entry : information.entrySet()) {
      message.replace("$" + entry.getKey(), entry.getValue().toString());
    }
    var channel = jda.getChannelById(TextChannel.class, channelIdentifier);
    channel.sendMessage(message);
    return buildInformation(channel);
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("channelIdentifier", channelIdentifier);
    information.put("channelName", channel.getName());
    information.put("message", message);
    return information;
  }
}
