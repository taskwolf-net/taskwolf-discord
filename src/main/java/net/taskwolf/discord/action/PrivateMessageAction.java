package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.taskwolf.core.action.Action;
import org.json.JSONObject;

import java.util.Map;

@AllArgsConstructor(staticName = "create")
public class PrivateMessageAction implements Action {
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
