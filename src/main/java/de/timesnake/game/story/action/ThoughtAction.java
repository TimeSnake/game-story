/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

public class ThoughtAction extends TriggeredAction {

  public static final String NAME = "thought";

  private final List<Supplier<String>> messages;
  private int messageIndex = 0;

  private boolean delaying = false;

  protected ThoughtAction(int id, StoryAction next, List<Supplier<String>> messages) {
    super(id, next);
    this.messages = messages;
  }

  public ThoughtAction(Quest quest, Toml action, int id, List<Integer> diaryPages) {
    super(id, diaryPages);

    this.messages = action.getList(MESSAGES).stream().map(m -> quest.parseString(((String) m)))
        .toList();
  }

  @Override
  public StoryAction clone(Quest section, StoryReader reader, StoryAction clonedNext,
      StoryChapter chapter) {
    return new ThoughtAction(this.id, clonedNext, this.messages);
  }

  private void nextMessage() {
    if (this.messageIndex >= this.messages.size()) {
      this.reader.forEach(UserPlayerDelegation::resetTitle);
      this.startNext();
      return;
    }

    this.reader.forEach(UserPlayerDelegation::resetTitle);
    this.reader.forEach(u -> u.showTitle(Component.empty(),
        Component.text(this.messages.get(this.messageIndex).get()), Duration.ofSeconds(20)));

    this.messageIndex++;
  }

  @StoryEvent
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
    StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

    if (user == null) {
      return;
    }

    if (!user.isSneaking()) {
      return;
    }

    if (!this.reader.containsUser(user)) {
      return;
    }

    if (this.delaying) {
      return;
    }

    this.delaying = true;

    if (this.isActive() && this.messageIndex > 0) {
      this.nextMessage();
    }

    Server.runTaskLaterSynchrony(() -> this.delaying = false, 20, GameStory.getPlugin());
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    if (this.messageIndex == 0) {
      this.nextMessage();
    }
  }
}
