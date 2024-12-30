package com.dulno.discord.action.channel.delete;

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
public final class ChannelDeleteAction implements Action<ChannelDeleteActionExecutor> {
  public static ChannelDeleteAction create(
    JDA jda, GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("channel", DatabaseDataType.TEXT));
    return new ChannelDeleteAction(jda, guildDatabaseTable, guildComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_channel_delete", contentColumns));
  }

  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-channel-delete-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.channel.delete.name")
      .withDescription("discord.action.channel.delete.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.delete.input.server.name",
        "guildIdentifier", "discord.action.channel.delete.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.delete.input.channel.name",
        "channelIdentifier", "discord.action.channel.delete.input.channel.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.create.output.channel.identifier",
        "channelIdentifier")).build();
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
      content.get("guildIdentifier"), content.get("channelIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(2).stringValue(),
        "channelIdentifier", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<ChannelDeleteActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      ChannelDeleteActionExecutor.create(jda, guildDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}