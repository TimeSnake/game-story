/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import java.util.List;

public class TriggerAction extends TriggeredAction {

  public static final String NAME = "trigger";

  public TriggerAction(int id, StoryAction next) {
    super(id, next);
  }

  public TriggerAction(Quest quest, Toml action, int id, List<Integer> diaryPages) {
    super(id, diaryPages);
  }

  @Override
  public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
      StoryChapter chapter) {
    return new TriggerAction(this.id, clonedNext);
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    super.startNext();
  }
}
