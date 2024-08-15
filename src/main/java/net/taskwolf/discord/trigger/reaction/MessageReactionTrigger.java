package net.taskwolf.discord.trigger.reaction;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerContentDatabaseTable;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class MessageReactionTrigger implements Trigger {
  public static MessageReactionTrigger create(
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("message", DatabaseDataType.TEXT));
    return new MessageReactionTrigger(guildComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_discord_message_reaction", contentColumns));
  }

  private final InputComponentSelect guildComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-message-reaction-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("discord.trigger.message.reaction.name")
      .withDescription("discord.trigger.message.reaction.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.message.reaction.input.server.name",
        "guildIdentifier", "discord.trigger.message.reaction.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.trigger.message.reaction.input.message.name",
        "messageIdentifier", "discord.trigger.message.reaction.input.message.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.message.reaction.output.user.identifier",
        "userIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.message.reaction.output.user.name",
        "userName"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.message.reaction.output.reaction.emoji",
        "reactionEmoji")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("message");
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(
      content.get("guildIdentifier"), content.get("messageIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(1).stringValue(),
        "messageIdentifier", row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(String condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}