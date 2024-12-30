package com.dulno.discord.action.channel.create;

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
public final class ChannelCreateAction implements Action<ChannelCreateActionExecutor> {
  public static ChannelCreateAction create(
    JDA jda, GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    return new ChannelCreateAction(jda, guildDatabaseTable, guildComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_channel_create", contentColumns));
  }

  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-channel-create-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.channel.create.name")
      .withDescription("discord.action.channel.create.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.create.input.server.name",
        "guildIdentifier", "discord.action.channel.create.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.create.input.name.name",
        "name", "discord.action.channel.create.input.name.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.create.output.channel.identifier",
        "channelIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.create.output.channel.name",
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
      content.get("guildIdentifier"), content.get("name")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(2).stringValue(),
        "name", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<ChannelCreateActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      ChannelCreateActionExecutor.create(jda, guildDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}