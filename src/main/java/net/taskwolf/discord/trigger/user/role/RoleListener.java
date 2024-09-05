package net.taskwolf.discord.trigger.user.role;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.database.condition.DatabaseCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class RoleListener extends ListenerAdapter {
  private final CoreModule coreModule;
  private final JDA jda;

  @Override
  public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
    super.onGuildMemberRoleAdd(event);
    if (event.getUser().equals(jda.getSelfUser())) {
      return;
    }
    coreModule.triggerWorkflows("discord", "discord-user-role-add-trigger",
      DatabaseCondition.of("guild", event.getGuild().getId()),
      userRoleInformation(event.getMember(), event.getRoles().getFirst()));
  }

  @Override
  public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
    super.onGuildMemberRoleRemove(event);
    if (event.getUser().equals(jda.getSelfUser())) {
      return;
    }
    coreModule.triggerWorkflows("discord", "discord-user-role-remove-trigger",
      DatabaseCondition.of("guild", event.getGuild().getId()),
      userRoleInformation(event.getMember(), event.getRoles().getFirst()));
  }

  private Map<String, Object> userRoleInformation(Member member, Role role) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", member.getUser().getId());
    information.put("userName", member.getUser().getName());
    information.put("roleIdentifier", role.getId());
    information.put("roleName", role.getName());
    return information;
  }
}