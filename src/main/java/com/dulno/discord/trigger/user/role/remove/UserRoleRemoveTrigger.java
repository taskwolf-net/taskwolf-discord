package com.dulno.discord.trigger.user.role.remove;

import com.dulno.discord.guild.GuildDatabaseTable;
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
public final class UserRoleRemoveTrigger implements Trigger {
  public static UserRoleRemoveTrigger create(
    GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    return new UserRoleRemoveTrigger(guildDatabaseTable, guildComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_discord_user_role_remove", contentColumns));
  }

  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-user-role-remove-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("discord.trigger.user.role.remove.name")
      .withDescription("discord.trigger.user.role.remove.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.user.role.remove.input.server.name",
        "guildIdentifier", "discord.trigger.user.role.remove.input.server.description", guildComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.remove.output.user.identifier",
        "userIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.remove.output.user.name",
        "userName"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.remove.output.role.identifier",
        "roleIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.remove.output.role.name",
        "roleName")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("guild");
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID triggerId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(ownerId,
      content.get("guildIdentifier")));
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
      Map.of("guildIdentifier", row.findCell(2).stringValue()));
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
