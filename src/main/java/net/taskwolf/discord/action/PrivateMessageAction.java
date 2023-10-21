package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.workflow.component.ComponentType;
import net.taskwolf.core.workflow.component.ComponentVariable;
import org.json.JSONObject;

import java.util.Map;

@AllArgsConstructor(staticName = "create")
public class PrivateMessageAction implements Action {
  public static ActionInformation information() {
    return ActionInformation.builder()
      .withName("Send private message")
      .withDescription("Send private message")
      .withIdentifier("discord-private-message-action")
      .withInputVariable(ComponentVariable.create("User Identifier",
        "userIdentifier", ComponentType.TEXT))
      .withInputVariable(ComponentVariable.create("Message",
        "message", ComponentType.TEXT))
      .withOutputVariable(ComponentVariable.create("User Name",
        "userName", ComponentType.TEXT)).build();
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
    information.put("userName", user.getName());
    information.put("message", message);
    return information;
  }
}
