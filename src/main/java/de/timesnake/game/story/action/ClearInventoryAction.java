/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import java.util.List;

public class ClearInventoryAction extends TriggeredAction {

  public static final String NAME = "clear_inventory";

  protected ClearInventoryAction(int id, StoryAction next) {
    super(id, next);
  }

  public ClearInventoryAction(Quest quest, Toml action, int id, List<Integer> diaryPages) {
    super(id, diaryPages);
  }

  @Override
  public StoryAction clone(Quest section, StoryReader reader, StoryAction clonedNext,
      StoryChapter chapter) {
    return new ClearInventoryAction(this.id, clonedNext);
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    this.reader.forEach(User::clearInventory);
    this.startNext();
  }
}
