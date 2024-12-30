package com.dulno.discord.action.channel.message;

import com.dulno.discord.guild.GuildDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action<ChannelMessageActionExecutor> {
  public static ChannelMessageAction create(
    JDA jda, GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    InputComponentSelect channelComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("channel", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("message", DatabaseDataType.TEXT));
    return new ChannelMessageAction(jda, guildDatabaseTable, guildComponentSelect,
      channelComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_channel_message", contentColumns));
  }

  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final InputComponentSelect channelComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-channel-message-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.channel.message.name")
      .withDescription("discord.action.channel.message.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.message.input.server.name",
        "guildIdentifier", "discord.action.channel.message.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.message.input.channel.name",
        "channelIdentifier", "discord.action.channel.message.input.channel.description", channelComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.message.input.message.name",
        "message", "discord.action.channel.message.input.message.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.message.output.message", "message"))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.message.output.channel.name",
        "channelName")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("guildIdentifier"), content.get("channelIdentifier"),
      content.get("message")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(2).stringValue(),
        "channelIdentifier", row.findCell(3).stringValue(),
        "message", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<ChannelMessageActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      ChannelMessageActionExecutor.create(jda, guildDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
