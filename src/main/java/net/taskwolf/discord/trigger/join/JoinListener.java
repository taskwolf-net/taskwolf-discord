package net.taskwolf.discord.trigger.join;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class JoinListener extends ListenerAdapter {
  private final CoreModule coreModule;
  private final JDA jda;

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    if (event.getUser().equals(jda.getSelfUser())) {
      return;
    }
    coreModule.triggerWorkflows("discord", "discord-user-join-trigger",
      "guild='" + event.getGuild().getId() + "'",
      userJoinInformation(event));
  }

  private Map<String, Object> userJoinInformation(GuildMemberJoinEvent event) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", event.getUser().getId());
    information.put("userName", event.getUser().getName());
    return information;
  }
}