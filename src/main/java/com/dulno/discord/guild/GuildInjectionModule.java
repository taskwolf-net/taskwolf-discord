package com.dulno.discord.guild;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class GuildInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  GuildDatabaseTable provideGuildDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var guildDatabaseTable = GuildDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    guildDatabaseTable.createIfNotExists();
    guildDatabaseTable.createIndexIfNotExists("guilds");
    return guildDatabaseTable;
  }
}
