package net.taskwolf.discord.trigger.message.reaction;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.workflow.WorkflowModule;
import net.taskwolf.core.database.condition.DatabaseCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class ReactionListener extends ListenerAdapter {
  private final WorkflowModule workflowModule;
  private final JDA jda;

  @Override
  public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
    super.onMessageReactionAdd(event);
    if (event.getUser().equals(jda.getSelfUser())) {
      return;
    }
    workflowModule.triggerWorkflows("discord", "discord-message-reaction-trigger",
      DatabaseCondition.of("message", event.getMessageId()),
      messageReactionInformation(event));
  }

  private Map<String, Object> messageReactionInformation(MessageReactionAddEvent event) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", event.getUser().getId());
    information.put("userName", event.getUser().getName());
    information.put("reactionEmoji", event.getEmoji().getFormatted());
    return information;
  }
}