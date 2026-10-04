package net.taskwolf.discord;

import net.taskwolf.core.environment.TaskwolfEnvironment;
import net.taskwolf.discord.action.channel.embed.ChannelEmbedAction;
import net.taskwolf.discord.trigger.channel.message.ChannelMessageTrigger;
import net.taskwolf.discord.trigger.channel.message.MessageListener;
import net.taskwolf.discord.trigger.message.reaction.MessageReactionTrigger;
import net.taskwolf.discord.trigger.message.reaction.ReactionListener;
import net.taskwolf.discord.trigger.user.join.JoinListener;
import net.taskwolf.discord.trigger.user.join.UserJoinTrigger;
import net.taskwolf.discord.trigger.user.mention.MentionListener;
import net.taskwolf.discord.trigger.user.mention.UserMentionTrigger;
import net.taskwolf.discord.trigger.user.role.RoleListener;
import net.taskwolf.discord.trigger.user.role.add.UserRoleAddTrigger;
import net.taskwolf.workflow.WorkflowModule;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.workflow.integration.Integration;
import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.workflow.trigger.TriggerRepository;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.discord.action.channel.create.ChannelCreateAction;
import net.taskwolf.discord.action.channel.delete.ChannelDeleteAction;
import net.taskwolf.discord.action.channel.message.ChannelMessageAction;
import net.taskwolf.discord.action.user.retrieve.UserRetrieveAction;
import net.taskwolf.discord.action.user.role.add.UserRoleAddAction;
import net.taskwolf.discord.action.user.role.remove.UserRoleRemoveAction;
import net.taskwolf.discord.guild.GuildDatabaseTable;
import net.taskwolf.discord.select.ChannelSelect;
import net.taskwolf.discord.select.GuildSelect;
import net.taskwolf.discord.select.RoleSelect;
import net.taskwolf.discord.trigger.user.role.remove.UserRoleRemoveTrigger;
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
    var configuration = DiscordConfiguration.createAndLoad();
    discordBot = DiscordBot.create(log, configuration);
    discordBot.initialize();
    var jda = discordBot.jda();
    var guildDatabaseTable = injector().getInstance(GuildDatabaseTable.class);
    accountLink = DiscordAccountLink.create(jda, guildDatabaseTable,
      configuration, injector().getInstance(TaskwolfEnvironment.class));
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