package net.taskwolf.discord.trigger;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.core.trigger.TriggerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public final class MessageListener extends ListenerAdapter {
  private final CoreModule coreModule;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final TriggerFactory triggerFactory;
  private final Distribution distribution;

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    super.onMessageReceived(event);
    var information = Maps.<String, Object>newHashMap();
    information.put("message", event.getMessage());
    information.put("messageAuthorIdentifier", event.getAuthor().getId());
    information.put("messageAuthorName", event.getAuthor().getName());
    findChannelMessageTriggers(event).thenAccept(entries ->
      executeWorkflows(entries, information));
    findPrivateMessageTriggers(event).thenAccept(entries ->
      executeWorkflows(entries, information));
  }

  private CompletableFuture<List<TriggerEntry>> findChannelMessageTriggers(
    MessageReceivedEvent event
  ) {
    return findTriggerEntries("discord-channel-message")
      .thenApply(entries -> entries.stream().filter(entry ->
        channelMessageSuitability(entry, event)).collect(Collectors.toList()));
  }

  private boolean channelMessageSuitability(
    TriggerEntry trigger, MessageReceivedEvent event
  ) {
    if (event.getChannelType() != ChannelType.TEXT) {
      return false;
    }
    var channelMessageTrigger = (ChannelMessageTrigger)
      triggerFactory.create(trigger.type(), trigger.content());
    return event.getGuild().getId().equals(channelMessageTrigger.guildIdentifier()) &&
      event.getChannel().getId().equals(channelMessageTrigger.channelIdentifier());
  }

  private CompletableFuture<List<TriggerEntry>> findPrivateMessageTriggers(
    MessageReceivedEvent event
  ) {
    return findTriggerEntries("discord-private-message")
      .thenApply(entries -> entries.stream().filter(entry ->
        privateMessageSuitability(entry, event)).collect(Collectors.toList()));
  }

  private boolean privateMessageSuitability(
    TriggerEntry trigger, MessageReceivedEvent event
  ) {
    if (event.getChannelType() != ChannelType.PRIVATE) {
      return false;
    }
    var privateMessageTrigger = (PrivateMessageTrigger)
      triggerFactory.create(trigger.type(), trigger.content());
    return event.getAuthor().getId().equals(privateMessageTrigger.authorIdentifier());
  }

  private CompletableFuture<List<TriggerEntry>> findTriggerEntries(
    String triggerType
  ) {
    return triggerDatabaseTable.findTriggersByModuleAndType("discord", triggerType)
      .thenApply(entries -> entries.stream().filter(entry ->
        distribution.isAssignedUser("discord", entry.ownerId())).collect(Collectors.toList()));
  }

  private void executeWorkflows(
    List<TriggerEntry> entries, Map<String, Object> information
  ) {
    for (var entry : entries) {
      coreModule.createWorkflow(entry.id()).thenAccept(workflow ->
        workflow.trigger(information));
    }
  }
}
