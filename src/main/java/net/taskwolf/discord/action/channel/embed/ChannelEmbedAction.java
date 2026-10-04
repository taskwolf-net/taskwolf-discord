package net.taskwolf.discord.action.channel.embed;

import net.taskwolf.core.database.*;
import net.taskwolf.discord.guild.GuildDatabaseTable;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelEmbedAction implements Action<ChannelEmbedActionExecutor> {
  public static ChannelEmbedAction create(
    JDA jda, GuildDatabaseTable guildDatabaseTable,
    InputComponentSelect guildComponentSelect,
    InputComponentSelect channelComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("guild", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("channel", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("color", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("url", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("authorName", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("authorUrl", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("authorIcon", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("fields", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("thumbnail", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("image", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("footerText", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("footerIcon", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("showTimestamp", DatabaseDataType.BOOLEAN));
    return new ChannelEmbedAction(jda, guildDatabaseTable, guildComponentSelect,
      channelComponentSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_discord_channel_embed", contentColumns));
  }

  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final InputComponentSelect guildComponentSelect;
  private final InputComponentSelect channelComponentSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "discord-channel-embed-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("discord.action.channel.embed.name")
      .withDescription("discord.action.channel.embed.description")
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.embed.input.server.name",
        "guildIdentifier", "discord.action.channel.embed.input.server.description", guildComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("discord.action.channel.embed.input.channel.name",
        "channelIdentifier", "discord.action.channel.embed.input.channel.description", channelComponentSelect))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.title.name",
        "title", "discord.action.channel.embed.input.title.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.description.name",
        "description", "discord.action.channel.embed.input.description.description", InputComponentDataType.TEXT_AREA))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.embed.input.color.name",
        "color", "discord.action.channel.embed.input.color.description", InputComponentDataType.COLOR))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.url.name",
        "url", "discord.action.channel.embed.input.url.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.author.name.name",
        "authorName", "discord.action.channel.embed.input.author.name.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.author.url.name",
        "authorUrl", "discord.action.channel.embed.input.author.url.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.author.icon.name",
        "authorIcon", "discord.action.channel.embed.input.author.icon.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.fields.name",
        "fields", "discord.action.channel.embed.input.fields.description", InputComponentDataType.MAP))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.thumbnail.name",
        "thumbnail", "discord.action.channel.embed.input.thumbnail.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.image.name",
        "image", "discord.action.channel.embed.input.image.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.footer.text.name",
        "footerText", "discord.action.channel.embed.input.footer.text.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("discord.action.channel.embed.input.footer.icon.name",
        "footerIcon", "discord.action.channel.embed.input.footer.icon.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("discord.action.channel.embed.input.timestamp.name",
        "showTimestamp", "discord.action.channel.embed.input.timestamp.description", InputComponentDataType.BOOLEAN))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    var title = content.get("title");
    var description = content.get("description");
    var color = content.get("color");
    var url = content.get("url");
    var authorName = content.get("authorName");
    var authorUrl = content.get("authorUrl");
    var authorIcon = content.get("authorIcon");
    var fields = content.get("fields");
    var thumbnail = content.get("thumbnail");
    var image = content.get("image");
    var footerText = content.get("footerText");
    var footerIcon = content.get("footerIcon");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("guildIdentifier"), content.get("channelIdentifier"),
      title == null ? "" : title, description == null ? "" : description,
      color == null ? "" : color, url == null ? "" : url,
      authorName == null ? "" : authorName, authorUrl == null ? "" : authorUrl,
      authorIcon == null ? "" : authorIcon, fields == null ? "" : fields,
      thumbnail == null ? "" : thumbnail, image == null ? "" : image,
      footerText == null ? "" : footerText, footerIcon == null ? "" : footerIcon,
      Boolean.parseBoolean((String) content.get("showTimestamp"))));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(this::buildContent);
  }

  private Map<String, Object> buildContent(DatabaseRow row) {
    var content = Maps.<String, Object>newHashMap();
    content.put("guildIdentifier", row.findCell(2).stringValue());
    content.put("channelIdentifier", row.findCell(3).stringValue());
    content.put("title", row.findCell(4).stringValue());
    content.put("description", row.findCell(5).stringValue());
    content.put("color", row.findCell(6).stringValue());
    content.put("url", row.findCell(7).stringValue());
    content.put("authorName", row.findCell(8).stringValue());
    content.put("authorUrl", row.findCell(9).stringValue());
    content.put("authorIcon", row.findCell(10).stringValue());
    content.put("fields", row.findCell(11).stringValue());
    content.put("thumbnail", row.findCell(12).stringValue());
    content.put("image", row.findCell(13).stringValue());
    content.put("footerText", row.findCell(14).stringValue());
    content.put("footerIcon", row.findCell(15).stringValue());
    content.put("showTimestamp", String.valueOf(row.findCell(16).booleanValue()));
    return content;
  }

  @Override
  public CompletableFuture<ChannelEmbedActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      ChannelEmbedActionExecutor.create(jda, guildDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue(),
        content.findCell(5).stringValue(), content.findCell(6).stringValue(),
        content.findCell(7).stringValue(), content.findCell(8).stringValue(),
        content.findCell(9).stringValue(), content.findCell(10).stringValue(),
        content.findCell(11).stringValue(), content.findCell(12).stringValue(),
        content.findCell(13).stringValue(), content.findCell(14).stringValue(),
        content.findCell(15).stringValue(), content.findCell(16).booleanValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}