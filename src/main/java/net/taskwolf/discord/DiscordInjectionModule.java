package net.taskwolf.discord;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import net.taskwolf.discord.guild.GuildInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public class DiscordInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(GuildInjectionModule.create());
  }
}
