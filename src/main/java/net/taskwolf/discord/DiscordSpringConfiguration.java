package net.taskwolf.discord;

import jakarta.annotation.PostConstruct;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.discord.access.GuildDatabaseTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordSpringConfiguration {
  @Autowired
  DatabaseConnection databaseConnection;
  @Autowired
  DatabaseKeyspace databaseKeyspace;
  private GuildDatabaseTable guildDatabaseTable;

  @Bean
  GuildDatabaseTable provideGuildDatabaseTable() {
    return guildDatabaseTable;
  }

  @PostConstruct
  private void initializeGuildDatabaseTable() {
    guildDatabaseTable = GuildDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    guildDatabaseTable.createIfNotExists();
    guildDatabaseTable.createIndexIfNotExists("guilds");
  }
}
