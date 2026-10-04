package net.taskwolf.discord.action.user.role.remove;

import net.taskwolf.discord.guild.GuildDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class UserRoleRemoveAction implements Action<UserRoleRemoveActionExecutor> {
  public static UserRoleRemoveAction create(
    JDA jda, GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    InputComponentSelect roleComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("user", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("role", DatabaseDataType.TEXT));
    return new UserRoleRemoveAction(jda, guildDatabaseTable, guildComponentSelect,
      roleComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_user_role_remove", contentColumns));
  }

  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
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
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("guildIdentifier"), content.get("userIdentifier"),
      content.get("roleIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(2).stringValue(),
        "userIdentifier", row.findCell(3).stringValue(),
        "roleIdentifier", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<UserRoleRemoveActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      UserRoleRemoveActionExecutor.create(jda, guildDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}