package net.taskwolf.discord.trigger.channel.message;

import net.taskwolf.discord.guild.GuildDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.workflow.trigger.Trigger;
import net.taskwolf.workflow.trigger.TriggerContentDatabaseTable;
import net.taskwolf.workflow.trigger.TriggerInformation;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ChannelMessageTrigger implements Trigger {
  public static ChannelMessageTrigger create(
    GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    InputComponentSelect channelComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("channel", DatabaseDataType.TEXT));
    return new ChannelMessageTrigger(guildDatabaseTable, guildComponentSelect,
      channelComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_discord_channel_message", contentColumns));
  }

  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final InputComponentSelect channelComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-channel-message-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("discord.trigger.channel.message.name")
      .withDescription("discord.trigger.channel.message.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.channel.message.input.server.name",
        "guildIdentifier", "discord.trigger.channel.message.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.channel.message.input.channel.name",
        "channelIdentifier", "discord.trigger.channel.message.input.channel.description", channelComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message",
        "message"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message.author.identifier",
        "messageAuthorIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.channel.message.output.message.author.name",
        "messageAuthorName")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("channel");
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID triggerId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(ownerId,
      content.get("guildIdentifier"), content.get("channelIdentifier")));
  }

  @Override
  public CompletableFuture<Boolean> checkExecution(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId)
      .thenCompose(row -> guildDatabaseTable.userGuildsExists(
        row.findCell(1).uuidValue())
        .thenCompose(exists -> checkExecution(row.findCell(1).uuidValue(),
          row.findCell(2).stringValue(), exists)));
  }

  public CompletableFuture<Boolean> checkExecution(
    UUID ownerId, String guildId, boolean hasGuilds
  ) {
    if (!hasGuilds) {
      return CompletableFuture.completedFuture(false);
    }
    return guildDatabaseTable.findGuilds(ownerId)
      .thenApply(guilds -> guilds.contains(guildId));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(2).stringValue(),
        "channelIdentifier", row.findCell(3).stringValue()));
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
