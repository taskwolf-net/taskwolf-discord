package com.dulno.discord.action.user.role.remove;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class UserRoleRemoveAction implements Action<UserRoleRemoveActionExecutor> {
  public static UserRoleRemoveAction create(
    JDA jda, InputComponentSelect guildComponentSelect,
    InputComponentSelect roleComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("user", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("role", DatabaseDataType.TEXT));
    return new UserRoleRemoveAction(jda, guildComponentSelect, roleComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_user_role_remove", contentColumns));
  }

  private final JDA jda;
  private final InputComponentSelect guildComponentSelect;
  private final InputComponentSelect roleComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-user-role-remove-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.user.role.remove.name")
      .withDescription("discord.action.user.role.remove.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.user.role.remove.input.server.name",
        "guildIdentifier", "discord.action.user.role.remove.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.user.role.remove.input.user.name",
        "userIdentifier", "discord.action.user.role.remove.input.user.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createSelect("discord.action.user.role.remove.input.role.name",
        "roleIdentifier", "discord.action.user.role.remove.input.role.description", roleComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("discord.action.user.role.remove.output.user.identifier",
        "userIdentifier")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("guildIdentifier"), content.get("userIdentifier"),
      content.get("roleIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(1).stringValue(),
        "userIdentifier", row.findCell(2).stringValue(),
        "roleIdentifier", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<UserRoleRemoveActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      UserRoleRemoveActionExecutor.create(jda, content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}