package com.dulno.discord.action.user.role.add;


import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.discord.guild.GuildDatabaseTable;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class UserRoleAddActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final UUID ownerId;
  private final String guildIdentifier;
  private String userIdentifier;
  private final String roleIdentifier;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var placeholderDissolve = PlaceholderDissolve.create(information);
    userIdentifier = placeholderDissolve.dissolve(userIdentifier);
    return guildDatabaseTable.userGuildsExists(ownerId)
      .thenCompose(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(boolean hasGuilds) {
    if (!hasGuilds) {
      return ActionResult.futureFailure("discord.action.user.role.add.failure.server.not.found");
    }
    return guildDatabaseTable.findGuilds(ownerId)
      .thenCompose(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(List<String> guilds) {
    if (!guilds.contains(guildIdentifier)) {
      return ActionResult.futureFailure("discord.action.user.role.add.failure.server.not.found");
    }
    return addUserRole();
  }

  private CompletableFuture<ActionResult> addUserRole() {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.user.role.add.failure.server.not.found");
    }
    var futureResponse = new CompletableFuture<ActionResult>();
    guild.retrieveMemberById(userIdentifier).queue(member ->
      futureResponse.complete(addUserRole(guild, member)));
    return futureResponse;
  }

  private ActionResult addUserRole(Guild guild, Member member) {
    if (member == null) {
      return ActionResult.failure("discord.action.user.role.add.failure.user.not.found");
    }
    var role = guild.getRoleById(roleIdentifier);
    if (role == null) {
      return ActionResult.failure("discord.action.user.role.add.failure.role.not.found");
    }
    try {
      guild.addRoleToMember(member, role).queue();
    } catch (Exception exception) {
      return ActionResult.failure("discord.action.user.role.add.failure.hierarchy");
    }
    return ActionResult.success(buildInformation(member.getUser()));
  }

  private Map<String, Object> buildInformation(User user) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", user.getId());
    return information;
  }
}