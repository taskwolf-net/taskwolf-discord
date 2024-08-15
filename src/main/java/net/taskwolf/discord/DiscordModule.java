package net.taskwolf.discord;

import com.google.inject.Injector;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerRepository;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.discord.action.channel.message.ChannelMessageAction;
import net.taskwolf.discord.guild.GuildDatabaseTable;
import net.taskwolf.discord.trigger.user.join.JoinListener;
import net.taskwolf.discord.trigger.user.join.UserJoinTrigger;
import net.taskwolf.discord.trigger.user.mention.MentionListener;
import net.taskwolf.discord.trigger.user.mention.UserMentionTrigger;
import net.taskwolf.discord.trigger.channel.message.ChannelMessageTrigger;
import net.taskwolf.discord.trigger.channel.message.MessageListener;
import net.taskwolf.discord.trigger.message.reaction.MessageReactionTrigger;
import net.taskwolf.discord.trigger.message.reaction.ReactionListener;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ModuleDescription(name = "discord", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DiscordModule extends Module {
  private Log log;
  private AccountLink accountLink;
  private InputComponentSelect guildComponentSelect;
  private InputComponentSelect channelComponentSelect;
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
    accountLink = DiscordAccountLink.create(jda, guildDatabaseTable);
    guildComponentSelect = createGuildComponentSelect(jda, guildDatabaseTable);
    channelComponentSelect = createChannelComponentSelect(jda);
    jda.addEventListener(JoinListener.create(
      injector().getInstance(CoreModule.class), jda));
    jda.addEventListener(MessageListener.create(
      injector().getInstance(CoreModule.class), jda));
    jda.addEventListener(MentionListener.create(
      injector().getInstance(CoreModule.class), jda));
    jda.addEventListener(ReactionListener.create(
      injector().getInstance(CoreModule.class), jda));
  }

  private InputComponentSelect createGuildComponentSelect(
    JDA jda, GuildDatabaseTable guildDatabaseTable
  ) {
    return (user, target, previousInputs) -> guildDatabaseTable.findGuilds(target)
      .thenApply(guilds -> guilds.stream().map(guild ->
        new JSONObject(Map.of("identifier", guild, "name",
          jda.getGuildById(guild).getName())).toString()).collect(Collectors.toList()));
  }

  private InputComponentSelect createChannelComponentSelect(JDA jda) {
    return (user, target, previousInputs) -> CompletableFuture.completedFuture(
      jda.getGuildById(previousInputs.get("guildIdentifier")).getTextChannels()
        .stream().map(channel -> new JSONObject(Map.of("identifier", channel.getId(),
          "name", channel.getName())).toString()).collect(Collectors.toList()));
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
    var repository = TriggerRepository.create();
    repository.registerTrigger(UserJoinTrigger.create(guildComponentSelect,
      databaseConnection, databaseKeyspace));
    repository.registerTrigger(ChannelMessageTrigger.create(guildComponentSelect,
      channelComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(UserMentionTrigger.create(guildComponentSelect,
      databaseConnection, databaseKeyspace));
    repository.registerTrigger(MessageReactionTrigger.create(guildComponentSelect,
      databaseConnection, databaseKeyspace));
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = ActionRepository.create();
    repository.registerAction(ChannelMessageAction.create(discordBot.jda(),
      guildComponentSelect, channelComponentSelect, databaseConnection,
      databaseKeyspace));
    return repository;
  }
}