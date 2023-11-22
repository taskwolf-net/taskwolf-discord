package net.taskwolf.discord.trigger;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
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
  private final TriggerFactory triggerFactory;
  private final JDA jda;

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    super.onMessageReceived(event);
    if (event.getAuthor().equals(jda.getSelfUser())) {
      return;
    }
    findChannelMessageTriggers(event).thenAccept(entries ->
      executeWorkflows(entries, channelMessageInformation(event)));
    findPrivateMessageTriggers(event).thenAccept(entries ->
      executeWorkflows(entries, privateMessageInformation(event)));
  }

  private CompletableFuture<List<TriggerEntry>> findChannelMessageTriggers(
    MessageReceivedEvent event
  ) {
    return coreModule.findTriggerEntries("discord", "discord-channel-message-trigger")
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
    return coreModule.findTriggerEntries("discord", "discord-private-message-trigger")
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

  private void executeWorkflows(
    List<TriggerEntry> entries, Map<String, Object> information
  ) {
    for (var entry : entries) {
      coreModule.createWorkflow(entry.id()).thenAccept(workflow ->
        workflow.trigger(information));
    }
  }

  private Map<String, Object> channelMessageInformation(MessageReceivedEvent event) {
    var information = Maps.<String, Object>newHashMap();
    information.put("guildIdentifier", event.getGuild().getId());
    information.put("channelIdentifier", event.getChannel().getId());
    information.put("message", event.getMessage().getContentRaw());
    information.put("messageAuthorIdentifier", event.getAuthor().getId());
    information.put("messageAuthorName", event.getAuthor().getName());
    return information;
  }

  private Map<String, Object> privateMessageInformation(MessageReceivedEvent event) {
    var information = Maps.<String, Object>newHashMap();
    information.put("authorIdentifier", event.getAuthor().getId());
    information.put("authorName", event.getAuthor().getName());
    information.put("message", event.getMessage().getContentRaw());
    return information;
  }
}
