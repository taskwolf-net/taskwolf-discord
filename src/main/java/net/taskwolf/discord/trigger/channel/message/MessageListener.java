package net.taskwolf.discord.trigger.channel.message;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.database.condition.DatabaseCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class MessageListener extends ListenerAdapter {
  private final CoreModule coreModule;
  private final JDA jda;

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    super.onMessageReceived(event);
    if (event.getAuthor().equals(jda.getSelfUser())) {
      return;
    }
    coreModule.triggerWorkflows("discord", "discord-channel-message-trigger",
      DatabaseCondition.of("channel", event.getChannel().getId()),
      channelMessageInformation(event));
  }

  private Map<String, Object> channelMessageInformation(MessageReceivedEvent event) {
    var information = Maps.<String, Object>newHashMap();
    information.put("message", event.getMessage().getContentRaw());
    information.put("messageAuthorIdentifier", event.getAuthor().getId());
    information.put("messageAuthorName", event.getAuthor().getName());
    return information;
  }
}
