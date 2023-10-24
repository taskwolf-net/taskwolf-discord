package net.taskwolf.discord;

import com.google.common.collect.Lists;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.discord.action.ChannelMessageAction;
import net.taskwolf.discord.action.DiscordActionFactory;
import net.taskwolf.discord.action.PrivateMessageAction;
import net.taskwolf.discord.trigger.ChannelMessageTrigger;
import net.taskwolf.discord.trigger.DiscordTriggerFactory;
import net.taskwolf.discord.trigger.MessageListener;
import net.taskwolf.discord.trigger.PrivateMessageTrigger;

import java.util.List;

@ModuleDescription(name = "discord", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DiscordModule extends Module {
  private Log log;
  private TriggerFactory triggerFactory;
  private ActionFactory actionFactory;
  private DiscordBot discordBot;

  public DiscordModule(CoreModule coreModule) {
    super(coreModule);
  }

  @Override
  public void enable() throws Exception {
    log = coreModule().log().subLog("Discord");
    discordBot = DiscordBot.create(log, DiscordConfiguration.createAndLoad());
    discordBot.initialize();
    triggerFactory = DiscordTriggerFactory.create();
    actionFactory = DiscordActionFactory.create(discordBot.jda());
    discordBot.jda().addEventListener(MessageListener.create(coreModule(),
      triggerFactory));
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
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Discord", "", "",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList(ChannelMessageTrigger.information(),
      PrivateMessageTrigger.information());
  }

  @Override
  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList(ChannelMessageAction.information(),
      PrivateMessageAction.information());
  }
}