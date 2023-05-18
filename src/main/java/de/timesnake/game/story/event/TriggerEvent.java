/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.event;

import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.listener.StoryEventListener;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

public abstract class TriggerEvent<Action extends TriggeredAction> implements StoryEventListener {

  protected Action action;

  protected TriggerEvent() {

  }

  public TriggerEvent(Action action) {
    this.action = action;
  }

  public void triggerAction(StoryUser user) {
    this.action.trigger(this.getType(), user);
  }

  public TriggerEvent<Action> clone(Quest section, StoryReader reader, Action clonedAction,
      StoryChapter chapter) {
    TriggerEvent<Action> cloned = this.clone(section, reader, chapter);
    cloned.action = clonedAction;
    return cloned;
  }

  public void start() {
    StoryServer.getEventManager().registerListeners(this);
  }

  public void stop() {
    StoryServer.getEventManager().unregisterListeners(this);
  }

  protected abstract TriggerEvent<Action> clone(Quest section, StoryReader reader,
      StoryChapter chapter);

  public abstract Type getType();

  public enum Type {
    AREA,
    SNEAK,
    DROP_ITEM,
    DROP_ITEM_AT,
    START,
    SLEEP,
    CHAT_CODE,
    DELAY
  }
}
