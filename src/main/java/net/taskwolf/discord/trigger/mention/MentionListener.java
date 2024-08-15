package net.taskwolf.discord.trigger.mention;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor(staticName = "create")
public final class MentionListener extends ListenerAdapter {
  private final CoreModule coreModule;
  private final JDA jda;
  private final Pattern pattern = Pattern.compile("<@\\d{18}>");

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    super.onMessageReceived(event);
    if (event.getAuthor().equals(jda.getSelfUser())) {
      return;
    }
    var matcher = pattern.matcher(event.getMessage().getContentRaw());
    boolean matchFound = matcher.find();
    if (!matchFound) {
      return;
    }
    var target = matcher.group().replace("<@", "").replace(">", "");
    jda.retrieveUserById(target).queue(user ->
      coreModule.triggerWorkflows("discord", "discord-user-mention-trigger",
        "guild='" + event.getGuild().getId() + "'",
        userMentionInformation(event, user)));
  }

  private Map<String, Object> userMentionInformation(
    MessageReceivedEvent event, User target
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("channelIdentifier", event.getChannel().getId());
    information.put("channelName", event.getChannel().getName());
    information.put("targetIdentifier", target.getId());
    information.put("targetName", target.getName());
    information.put("message", event.getMessage().getContentRaw());
    information.put("messageAuthorIdentifier", event.getAuthor().getId());
    information.put("messageAuthorName", event.getAuthor().getName());
    return information;
  }
}
