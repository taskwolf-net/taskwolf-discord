package com.dulno.discord.trigger.user.mention;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.workflow.trigger.Trigger;
import com.dulno.workflow.trigger.TriggerContentDatabaseTable;
import com.dulno.workflow.trigger.TriggerInformation;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class UserMentionTrigger implements Trigger {
  public static UserMentionTrigger create(
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    return new UserMentionTrigger(guildComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_discord_user_mention", contentColumns));
  }

  private final InputComponentSelect guildComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-user-mention-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("discord.trigger.user.mention.name")
      .withDescription("discord.trigger.user.mention.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.user.mention.input.server.name",
        "guildIdentifier", "discord.trigger.user.mention.input.server.description", guildComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.channel.identifier",
        "channelIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.channel.name",
        "channelName"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.target.identifier",
        "targetIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.target.name",
        "targetName"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.message",
        "message"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.message.author.identifier",
        "messageAuthorIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.mention.output.message.author.name",
        "messageAuthorName")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("guild");
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(
      content.get("guildIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
