package net.taskwolf.discord.trigger.user.role.add;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerContentDatabaseTable;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class UserRoleAddTrigger implements Trigger {
  public static UserRoleAddTrigger create(
    InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    return new UserRoleAddTrigger(guildComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_discord_user_role_add", contentColumns));
  }

  private final InputComponentSelect guildComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-user-role-add-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("discord.trigger.user.role.add.name")
      .withDescription("discord.trigger.user.role.add.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.trigger.user.role.add.input.server.name",
        "guildIdentifier", "discord.trigger.user.role.add.input.server.description", guildComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.add.output.user.identifier",
        "userIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.add.output.user.name",
        "userName"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.add.output.role.identifier",
        "roleIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.trigger.user.role.add.output.role.name",
        "roleName")).build();
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
