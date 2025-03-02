package com.dulno.discord;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class DiscordConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/discord/discord.json";

  public static DiscordConfiguration createAndLoad() throws Exception {
    var configuration = new DiscordConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String clientId;
  private String token;
  private String activity;

  private DiscordConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    clientId = json.getString("clientId");
    token = json.getString("token");
    activity = json.getString("activity");
  }
}
