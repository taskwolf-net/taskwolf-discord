package net.taskwolf.discord.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
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
      .withInputVariable(InputComponentVariable.createRequired("Author Identifier",
        "authorIdentifier", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("Author Name", "authorName"))
      .withOutputVariable(OutputComponentVariable.create("Message", "message")).build();
  }

  public static PrivateMessageTrigger of(JSONObject content) {
    return create(content.getString("authorIdentifier"));
  }

  private final String authorIdentifier;
}
