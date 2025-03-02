package com.dulno.discord;

import com.dulno.core.environment.DulnoEnvironment;
import com.dulno.discord.action.channel.embed.ChannelEmbedAction;
import com.dulno.discord.trigger.channel.message.ChannelMessageTrigger;
import com.dulno.discord.trigger.channel.message.MessageListener;
import com.dulno.discord.trigger.message.reaction.MessageReactionTrigger;
import com.dulno.discord.trigger.message.reaction.ReactionListener;
import com.dulno.discord.trigger.user.join.JoinListener;
import com.dulno.discord.trigger.user.join.UserJoinTrigger;
import com.dulno.discord.trigger.user.mention.MentionListener;
import com.dulno.discord.trigger.user.mention.UserMentionTrigger;
import com.dulno.discord.trigger.user.role.RoleListener;
import com.dulno.discord.trigger.user.role.add.UserRoleAddTrigger;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.action.ActionRepository;
import com.dulno.workflow.integration.Integration;
import com.google.inject.Injector;
import com.dulno.core.account.AccountLink;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.workflow.trigger.TriggerRepository;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.discord.action.channel.create.ChannelCreateAction;
import com.dulno.discord.action.channel.delete.ChannelDeleteAction;
import com.dulno.discord.action.channel.message.ChannelMessageAction;
import com.dulno.discord.action.user.retrieve.UserRetrieveAction;
import com.dulno.discord.action.user.role.add.UserRoleAddAction;
import com.dulno.discord.action.user.role.remove.UserRoleRemoveAction;
import com.dulno.discord.guild.GuildDatabaseTable;
import com.dulno.discord.select.ChannelSelect;
import com.dulno.discord.select.GuildSelect;
import com.dulno.discord.select.RoleSelect;
import com.dulno.discord.trigger.user.role.remove.UserRoleRemoveTrigger;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "discord", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DiscordModule extends Integration {
  private Log log;
  private AccountLink accountLink;
  private InputComponentSelect guildSelect;
  private InputComponentSelect channelSelect;
  private InputComponentSelect roleSelect;
  private DiscordBot discordBot;

  public DiscordModule(Injector injector) {
    super(injector.createChildInjector(DiscordInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Discord");
    injector().getInstance(SpringApplication.class).addInitializers(
      injector().getInstance(DiscordContextInitializer.class));
    discordBot = DiscordBot.create(log, DiscordConfiguration.createAndLoad());
    discordBot.initialize();
    var jda = discordBot.jda();
    var guildDatabaseTable = injector().getInstance(GuildDatabaseTable.class);
    accountLink = DiscordAccountLink.create(jda, guildDatabaseTable,
      injector().getInstance(DulnoEnvironment.class));
    guildSelect = GuildSelect.create(guildDatabaseTable, jda);
    channelSelect = ChannelSelect.create(jda, guildDatabaseTable);
    roleSelect = RoleSelect.create(jda, guildDatabaseTable);
    var workflowModule = injector().getInstance(WorkflowModule.class);
    jda.addEventListener(JoinListener.create(workflowModule, jda));
    jda.addEventListener(RoleListener.create(workflowModule, jda));
    jda.addEventListener(MessageListener.create(workflowModule, jda));
    jda.addEventListener(MentionListener.create(workflowModule, jda));
    jda.addEventListener(ReactionListener.create(workflowModule, jda));
  }

  @Override
  public void disable() {
    discordBot.shutdown();
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
  public TriggerRepository triggerRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var guildDatabaseTable = injector().getInstance(GuildDatabaseTable.class);
    var repository = TriggerRepository.create();
    repository.registerTrigger(ChannelMessageTrigger.create(guildDatabaseTable,
      guildSelect, channelSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(UserJoinTrigger.create(guildDatabaseTable,
      guildSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(UserRoleAddTrigger.create(guildDatabaseTable,
      guildSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(UserRoleRemoveTrigger.create(guildDatabaseTable,
      guildSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(UserMentionTrigger.create(guildDatabaseTable,
      guildSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(MessageReactionTrigger.create(guildDatabaseTable,
      guildSelect, databaseConnection, databaseKeyspace));
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var guildDatabaseTable = injector().getInstance(GuildDatabaseTable.class);
    var repository = ActionRepository.create();
    repository.registerAction(ChannelMessageAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, channelSelect, databaseConnection,
      databaseKeyspace));
    repository.registerAction(ChannelEmbedAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, channelSelect, databaseConnection,
      databaseKeyspace));
    repository.registerAction(ChannelCreateAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, databaseConnection, databaseKeyspace));
    repository.registerAction(ChannelDeleteAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, databaseConnection, databaseKeyspace));
    repository.registerAction(UserRetrieveAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, databaseConnection, databaseKeyspace));
    repository.registerAction(UserRoleAddAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, roleSelect, databaseConnection,
      databaseKeyspace));
    repository.registerAction(UserRoleRemoveAction.create(discordBot.jda(),
      guildDatabaseTable, guildSelect, roleSelect, databaseConnection,
      databaseKeyspace));
    return repository;
  }
}