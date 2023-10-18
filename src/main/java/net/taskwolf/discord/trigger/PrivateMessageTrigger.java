package net.taskwolf.discord.trigger;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class PrivateMessageTrigger implements Trigger {
  public static PrivateMessageTrigger of(String content) {
    return create("");
  }

  private final String authorIdentifier;
}
