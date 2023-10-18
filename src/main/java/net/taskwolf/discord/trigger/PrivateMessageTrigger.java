package net.taskwolf.discord.trigger;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class PrivateMessageTrigger implements Trigger {
  public static PrivateMessageTrigger of(JSONObject content) {
    return create(content.getString("authorIdentifier"));
  }

  private final String authorIdentifier;
}
