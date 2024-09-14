package com.dulno.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.discord.guild.GuildDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DiscordContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final GuildDatabaseTable guildDatabaseTable;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("guildDatabaseTable", guildDatabaseTable);
  }
}
