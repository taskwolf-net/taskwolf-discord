package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import org.json.JSONObject;

import java.util.Map;

@AllArgsConstructor(staticName = "create")
public class PrivateMessageAction implements Action {
  public static ActionInformation information() {
    return ActionInformation.builder()
      .withName("Send private message")
      .withDescription("Send private message")
      .withIdentifier("discord-private-message-action")
      .withInputVariable(InputComponentVariable.createRequired("User Identifier",
        "userIdentifier", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("Message",
        "message", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("User Identifier", "userIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("Message", "message"))
      .withOutputVariable(OutputComponentVariable.create("User Name", "userName")).build();
  }

  public static PrivateMessageAction of(JDA jda, JSONObject content) {
    return create(jda, content.getString("userIdentifier"),
      content.getString("message"));
  }

  private final JDA jda;
  private final String userIdentifier;
  private String message;

  @Override
  public Map<String, Object> execute(Map<String, Object> information) {
    for (var entry : information.entrySet()) {
      message = message.replace("$" + entry.getKey(), entry.getValue().toString());
    }
    var user = jda.getUserById(userIdentifier);
    user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message))
      .queue();
    return buildInformation(user);
  }

  private Map<String, Object> buildInformation(User user) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", userIdentifier);
    information.put("message", message);
    information.put("userName", user.getName());
    return information;
  }
}
