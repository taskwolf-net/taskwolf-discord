package net.taskwolf.discord.trigger;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerFactory;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public class DiscordTriggerFactory implements TriggerFactory {
  @Override
  public Trigger create(String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("discord-channel-message-trigger")) {
      return ChannelMessageTrigger.of(json);
    }
    if (type.equals("discord-private-message-trigger")) {
      return PrivateMessageTrigger.of(json);
    }
    return null;
  }
}
