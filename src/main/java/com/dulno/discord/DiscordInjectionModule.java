package com.dulno.discord;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import com.dulno.discord.guild.GuildInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public class DiscordInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(GuildInjectionModule.create());
  }
}
