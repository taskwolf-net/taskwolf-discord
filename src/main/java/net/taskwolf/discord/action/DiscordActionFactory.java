package net.taskwolf.discord.action;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionFactory;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public final class DiscordActionFactory implements ActionFactory {
  private final JDA jda;

  @Override
  public Action create(String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("discord-channel-message-action")) {
      return ChannelMessageAction.of(jda, json);
    }
    if (type.equals("discord-private-message-action")) {
      return PrivateMessageAction.of(jda, json);
    }
    return null;
  }
}
