package net.taskwolf.discord.action.user.role.add;


import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class UserRoleAddActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final String guildIdentifier;
  private final String roleIdentifier;
  private String userIdentifier;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.user.role.add.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    userIdentifier = placeholderDissolve.dissolve(userIdentifier);
    var futureResponse = new CompletableFuture<ActionResult>();
    guild.retrieveMemberById(userIdentifier).queue(member ->
      futureResponse.complete(execute(guild, member)));
    return futureResponse;
  }

  private ActionResult execute(Guild guild, Member member) {
    if (member == null) {
      return ActionResult.failure("discord.action.user.role.add.failure.user.not.found");
    }
    var role = guild.getRoleById(roleIdentifier);
    if (role == null) {
      return ActionResult.failure("discord.action.user.role.add.failure.role.not.found");
    }
    guild.addRoleToMember(member, role);
    return ActionResult.success(buildInformation(member.getUser()));
  }

  private Map<String, Object> buildInformation(User user) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", user.getId());
    return information;
  }
}