package net.taskwolf.discord.action.user.retrieve;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class UserRetrieveActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final String guildIdentifier;
  private String userIdentifier;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var guild = jda.getGuildById(guildIdentifier);
    if (guild == null) {
      return ActionResult.futureFailure("discord.action.user.retrieve.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    userIdentifier = placeholderDissolve.dissolve(userIdentifier);
    var futureResponse = new CompletableFuture<ActionResult>();
    guild.retrieveMemberById(userIdentifier).queue(member ->
      futureResponse.complete(execute(member)));
    return futureResponse;
  }

  private ActionResult execute(Member member) {
    if (member == null) {
      return ActionResult.failure("discord.action.user.retrieve.failure.user.not.found");
    }
    return ActionResult.success(buildInformation(member.getUser()));
  }

  private Map<String, Object> buildInformation(User user) {
    var information = Maps.<String, Object>newHashMap();
    information.put("userIdentifier", user.getId());
    information.put("userName", user.getName());
    information.put("userAvatarUrl", user.getAvatarUrl());
    return information;
  }
}