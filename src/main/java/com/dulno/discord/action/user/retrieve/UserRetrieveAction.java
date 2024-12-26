package com.dulno.discord.action.user.retrieve;

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
public final class UserRetrieveAction implements Action<UserRetrieveActionExecutor> {
  public static UserRetrieveAction create(
    JDA jda, InputComponentSelect guildComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("user", DatabaseDataType.TEXT));
    return new UserRetrieveAction(jda, guildComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_user_retrieve", contentColumns));
  }

  private final JDA jda;
  private final InputComponentSelect guildComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-user-retrieve-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.user.retrieve.name")
      .withDescription("discord.action.user.retrieve.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.user.retrieve.input.server.name",
        "guildIdentifier", "discord.action.user.retrieve.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.user.retrieve.input.user.name",
        "userIdentifier", "discord.action.user.retrieve.input.user.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.action.user.retrieve.output.user.identifier",
        "userIdentifier"))
      .withOutputVariable(OutputComponentVariable.create("discord.action.user.retrieve.output.user.name",
        "userName"))
      .withOutputVariable(OutputComponentVariable.create("discord.action.user.retrieve.output.user.avatar",
        "userAvatarUrl")).build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("guildIdentifier"), content.get("userIdentifier")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("guildIdentifier", row.findCell(1).stringValue(),
        "userIdentifier", row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<UserRetrieveActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      UserRetrieveActionExecutor.create(jda, content.findCell(1).stringValue(),
        content.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}