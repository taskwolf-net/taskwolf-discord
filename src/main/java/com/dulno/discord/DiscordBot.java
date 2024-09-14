package com.dulno.discord;

import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import com.dulno.core.log.Log;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DiscordBot {
  private final Log log;
  private final DiscordConfiguration discordConfiguration;
  @Getter
  private JDA jda;

  public void initialize() {
    ((LoggerContext) LoggerFactory.getILoggerFactory())
      .getLogger("net.dv8tion").setLevel(ch.qos.logback.classic.Level.ERROR);
    jda = JDABuilder.createDefault(discordConfiguration.token())
      .setActivity(Activity.customStatus(discordConfiguration.activity()))
      .setStatus(OnlineStatus.ONLINE)
      .enableIntents(GatewayIntent.GUILD_MEMBERS)
      .enableIntents(GatewayIntent.GUILD_MESSAGES)
      .enableIntents(GatewayIntent.MESSAGE_CONTENT)
      .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS)
      .build();
    jda.setAutoReconnect(true);
    log.log(Level.INFO, "Discord bot successfully connected");
  }

  public void shutdown() {
    jda.shutdown();
  }
}
