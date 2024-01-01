package net.taskwolf.discord.access;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.template.Template;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class GuildDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "discord_guild";

  public static GuildDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("guilds", DatabaseDataType.TEXT));
    return new GuildDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private GuildDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void addGuild(UUID userId, String guildId) {
    exists(DatabaseCell.create(userId)).thenAccept(exists ->
      addGuild(userId, guildId, exists));
  }

  private void addGuild(UUID userId, String guildId, boolean exists) {
    if (!exists) {
      insertUserGuild(userId, guildId);
      return;
    }
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      addGuild(userId, guildId, row));
  }

  private void addGuild(UUID userId, String guildId, DatabaseRow row) {
    var guildIds = row.findCell(1).<String>listValue();
    guildIds.add(guildId);
    updateUserGuilds(userId, guildIds);
  }

  private void insertUserGuild(UUID userId, String guildId) {
    insert(DatabaseRow.of(userId, Lists.newArrayList(guildId)));
  }

  public void removeGuild(UUID userId, String guildId) {
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      removeGuild(userId, guildId, row));
  }

  private void removeGuild(UUID userId, String guildId, DatabaseRow row) {
    var guildIds = row.findCell(1).<String>listValue();
    if (guildIds.size() == 1) {
      deleteUserGuilds(userId);
      return;
    }
    guildIds.remove(guildId);
    updateUserGuilds(userId, guildIds);
  }

  public void updateUserGuilds(UUID userId, List<String> guildIds) {
    update(DatabaseCell.create(userId), DatabaseRow.of(userId, guildIds));
  }

  public void deleteUserGuilds(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<Boolean> userGuildsExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public CompletableFuture<Integer> guildOccurNumber(String guildId) {
    return selectRows("guilds CONTAINS '" + guildId + "'")
      .thenApply(List::size);
  }

  public CompletableFuture<List<String>> findGuilds(UUID userId) {
    return selectRow(DatabaseCell.create(userId))
      .thenApply(row -> row.findCell(1).listValue());
  }
}
