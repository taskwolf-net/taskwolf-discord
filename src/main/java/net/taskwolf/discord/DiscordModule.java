package net.taskwolf.discord;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.discord.access.GuildDatabaseTable;
import net.taskwolf.discord.action.ChannelMessageAction;
import net.taskwolf.discord.action.DiscordActionFactory;
import net.taskwolf.discord.trigger.ChannelMessageTrigger;
import net.taskwolf.discord.trigger.DiscordTriggerFactory;
import net.taskwolf.discord.trigger.MessageListener;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ModuleDescription(name = "discord", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DiscordModule extends Module {
  private Log log;
  private GuildDatabaseTable guildDatabaseTable;
  private TriggerFactory triggerFactory;
  private ActionFactory actionFactory;
  private AccountLink accountLink;
  private InputComponentSelect guildComponentSelect;
  private InputComponentSelect channelComponentSelect;
  private DiscordBot discordBot;

  public DiscordModule(CoreModule coreModule) {
    super(coreModule);
  }

  @Override
  public void enable() throws Exception {
    log = coreModule().log().subLog("Discord");
    guildDatabaseTable = GuildDatabaseTable.create(coreModule().databaseConnection(),
      coreModule().databaseKeyspace());
    discordBot = DiscordBot.create(log, DiscordConfiguration.createAndLoad());
    discordBot.initialize();
    var jda = discordBot.jda();
    triggerFactory = DiscordTriggerFactory.create();
    actionFactory = DiscordActionFactory.create(jda);
    accountLink = DiscordAccountLink.create(jda, guildDatabaseTable);
    guildComponentSelect = createGuildComponentSelect(jda, guildDatabaseTable);
    channelComponentSelect = createChannelComponentSelect(jda);
    jda.addEventListener(MessageListener.create(coreModule(), triggerFactory, jda));
  }

  private InputComponentSelect createGuildComponentSelect(
    JDA jda, GuildDatabaseTable guildDatabaseTable
  ) {
    return (user, previousInputs) -> guildDatabaseTable.findGuilds(user)
      .thenApply(guilds -> guilds.stream().map(guild ->
        new JSONObject(Map.of("identifier", guild, "name",
          jda.getGuildById(guild).getName())).toString()).collect(Collectors.toList()));
  }

  private InputComponentSelect createChannelComponentSelect(JDA jda) {
    return (user, previousInputs) -> CompletableFuture.completedFuture(
      jda.getGuildById(previousInputs.get("guildIdentifier")).getTextChannels()
        .stream().map(channel -> new JSONObject(Map.of("identifier", channel.getId(),
          "name", channel.getName())).toString()).collect(Collectors.toList()));
  }

  @Override
  public void disable() {
    discordBot.shutdown();
  }

  @Override
  public TriggerFactory triggerFactory() {
    return triggerFactory;
  }

  @Override
  public ActionFactory actionFactory() {
    return actionFactory;
  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Discord", "", "discord.png",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList(ChannelMessageTrigger.information(guildComponentSelect,
      channelComponentSelect));
  }

  @Override
  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList(ChannelMessageAction.information(guildComponentSelect,
      channelComponentSelect));
  }
}