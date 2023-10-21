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
public final class PrivateMessageTrigger implements Trigger {
  public static TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("Receive new private message")
      .withDescription("Receive new private message")
      .withIdentifier("discord-private-message-trigger")
      .withInputVariable(ComponentVariable.create("Author Identifier",
        "authorIdentifier", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Message",
        "message", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("Author Name",
        "authorName", ComponentType.TEXT)).build();
  }

  public static PrivateMessageTrigger of(JSONObject content) {
    return create(content.getString("authorIdentifier"));
  }

  private final String authorIdentifier;
}
