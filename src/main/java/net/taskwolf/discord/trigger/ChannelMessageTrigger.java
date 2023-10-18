package net.taskwolf.discord.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ChannelMessageTrigger implements Trigger {
  public static ChannelMessageTrigger of(JSONObject content) {
    return create(content.getString("guildIdentifier"),
      content.getString("channelIdentifier"));
  }

  private final String guildIdentifier;
  private final String channelIdentifier;
}
