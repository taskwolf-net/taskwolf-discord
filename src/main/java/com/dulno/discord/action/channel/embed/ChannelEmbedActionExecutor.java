package com.dulno.discord.action.channel.embed;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.discord.guild.GuildDatabaseTable;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONObject;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChannelEmbedActionExecutor implements ActionExecutor {
  private final JDA jda;
  private final GuildDatabaseTable guildDatabaseTable;
  private final UUID ownerId;
  private final String guildIdentifier;
  private final String channelIdentifier;
  private String title;
  private String description;
  private final String color;
  private String url;
  private String authorName;
  private String authorUrl;
  private String authorIcon;
  private String fields;
  private String thumbnail;
  private String image;
  private String footerText;
  private String footerIcon;
  private final boolean showTimestamp;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var placeholderDissolve = PlaceholderDissolve.create(information);
    title = placeholderDissolve.dissolve(title);
    description = placeholderDissolve.dissolve(description);
    url = placeholderDissolve.dissolve(url);
    authorName = placeholderDissolve.dissolve(authorName);
    authorUrl = placeholderDissolve.dissolve(authorUrl);
    authorIcon = placeholderDissolve.dissolve(authorIcon);
    dissolveFields(information);
    thumbnail = placeholderDissolve.dissolve(thumbnail);
    image = placeholderDissolve.dissolve(image);
    footerText = placeholderDissolve.dissolve(footerText);
    footerIcon = placeholderDissolve.dissolve(footerIcon);
    return guildDatabaseTable.userGuildsExists(ownerId)
      .thenCompose(this::checkUserGuilds);
  }

  private void dissolveFields(Map<String, Object> information) {
    var formattedInformation = Maps.<String, Object>newHashMap();
    for (var entry : information.entrySet()) {
      formattedInformation.put(entry.getKey(),
        entry.getValue().toString().replace("\\", "\\\\")
          .replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f")
          .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"));
    }
    var dissolve = PlaceholderDissolve.create(formattedInformation);
    fields = dissolve.dissolve(fields);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(boolean hasGuilds) {
    if (!hasGuilds) {
      return ActionResult.futureFailure("discord.action.channel.embed.failure.server.not.found");
    }
    return guildDatabaseTable.findGuilds(ownerId)
      .thenComposeAsync(this::checkUserGuilds);
  }

  public CompletableFuture<ActionResult> checkUserGuilds(List<String> guilds) {
    if (!guilds.contains(guildIdentifier)) {
      return ActionResult.futureFailure("discord.action.channel.embed.failure.server.not.found");
    }
    return sendChannelEmbed();
  }

  public CompletableFuture<ActionResult> sendChannelEmbed() {
    if (jda.getGuildById(guildIdentifier) == null) {
      return ActionResult.futureFailure("discord.action.channel.embed.failure.server.not.found");
    }
    var channel = jda.getTextChannelById(channelIdentifier);
    if (channel == null) {
      return ActionResult.futureFailure("discord.action.channel.embed.failure.channel.not.found");
    }
    try {
      channel.sendMessageEmbeds(buildEmbed()).queue();
    } catch (Exception exception) {
      return ActionResult.futureFailure(exception.getMessage());
    }
    return ActionResult.futureSuccess(Maps.newHashMap());
  }

  private MessageEmbed buildEmbed() throws Exception {
    var builder = new EmbedBuilder();
    if (!title.isEmpty()) {
      builder.setTitle(title);
    }
    if (!description.isEmpty()) {
      builder.setDescription(description);
    }
    builder.setColor(Color.decode(color));
    if (!url.isEmpty()) {
      builder.setUrl(url);
    }
    if (!authorName.isEmpty()) {
      builder.setAuthor(authorName, authorUrl.isEmpty() ? null : authorUrl,
        authorIcon.isEmpty() ? null : authorIcon);
    }
    addEmbedMessageFields(builder);
    if (!thumbnail.isEmpty()) {
      builder.setThumbnail(thumbnail);
    }
    if (!image.isEmpty()) {
      builder.setImage(image);
    }
    if (!footerText.isEmpty()) {
      builder.setFooter(footerText, footerIcon.isEmpty() ? null : footerIcon);
    }
    if (showTimestamp) {
      builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
    }
    return builder.build();
  }

  private void addEmbedMessageFields(EmbedBuilder builder) {
    var parsedFields = new JSONObject(fields);
    var keys = Lists.newArrayList(parsedFields.keySet());
    for (var i = 0; i < keys.size(); i++) {
      var key = keys.get(i);
      var value = parsedFields.optString(key, "");
      if (!key.isEmpty() || !value.isEmpty()) {
        builder.addField(key, value, false);
        continue;
      }
      for (var j = i + 1; j < keys.size(); j++) {
        var nextKey = keys.get(j);
        var nextValue = parsedFields.optString(nextKey, "");
        if (!nextKey.isEmpty() || !nextValue.isEmpty()) {
          builder.addField(key, value, false);
          break;
        }
      }
    }
  }
}
