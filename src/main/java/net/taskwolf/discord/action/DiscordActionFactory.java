package net.taskwolf.discord.action;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionFactory;

@RequiredArgsConstructor(staticName = "create")
public final class DiscordActionFactory implements ActionFactory {
  private final JDA jda;

  @Override
  public Action create(String content) {
    return null;
  }
}
