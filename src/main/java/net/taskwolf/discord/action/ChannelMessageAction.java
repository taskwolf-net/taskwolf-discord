package net.taskwolf.discord.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action {
  public static ActionInformation information(
    InputComponentSelect guildComponentSelect, InputComponentSelect channelComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("discord.action.channel.message.name")
      .withDescription("discord.action.channel.message.description")
      .withIdentifier("discord-channel-message-action")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.message.input.server.name",
        "guildIdentifier", "discord.action.channel.message.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.message.input.channel.name",
        "channelIdentifier", "discord.action.channel.message.input.channel.description", channelComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.message.input.message.name",
        "message", "discord.action.channel.message.input.message.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.message.output.message", "message"))
      .withOutputVariable(OutputComponentVariable.create("discord.action.channel.message.output.channel.name",
        "channelName")).build();
  }

  public static ChannelMessageAction of(JDA jda, JSONObject content) {
    return create(jda, content.getString("guildIdentifier"),
      content.getString("channelIdentifier"), content.getString("message"));
  }

  private final JDA jda;
  private final String guildIdentifier;
  private final String channelIdentifier;
  private String message;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    if (jda.getGuildById(guildIdentifier) == null) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.server.not.found");
    }
    var placeholderDissolve = PlaceholderDissolve.create(information);
    message = placeholderDissolve.dissolve(message);
    var channel = jda.getTextChannelById(channelIdentifier);
    if (channel == null) {
      return ActionResult.futureFailure("discord.action.channel.message.failure.channel.not.found");
    }
    channel.sendMessage(message).queue();
    return ActionResult.futureSuccess(buildInformation(channel));
  }

  private Map<String, Object> buildInformation(TextChannel channel) {
    var information = Maps.<String, Object>newHashMap();
    information.put("message", message);
    information.put("channelName", channel.getName());
    return information;
  }
}
