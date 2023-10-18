package net.taskwolf.discord;

import com.google.common.collect.Lists;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.discord.trigger.MessageListener;

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
    log = log().subLog("Discord");
    discordBot = DiscordBot.create(log, DiscordConfiguration.createAndLoad());
    discordBot.initialize();
    discordBot.jda().addEventListener(MessageListener.create(coreModule(),
      triggerDatabaseTable(), triggerFactory, Lists.newArrayList()));
  }

  @Override
  public void disable() {
    discordBot.shutdown();
  }

  @Override
  public TriggerFactory triggerFactory() {
    return null;
  }

  @Override
  public ActionFactory actionFactory() {
    return null;
  }

  @Override
  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList();
  }

  @Override
  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList();
  }
}