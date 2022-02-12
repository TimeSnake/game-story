package de.timesnake.game.story.event;

import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.user.StoryUser;

public abstract class TriggerEvent<Action extends TriggeredAction> {

    protected Action action;

    protected TriggerEvent() {

    }

    public TriggerEvent(Action action) {
        this.action = action;
    }

    public boolean triggerAction(StoryUser user) {
        return this.action.trigger(this.getType(), user);
    }

    public TriggerEvent<Action> clone(StoryUser reader, Action clonedAction) {
        TriggerEvent<Action> cloned = this.clone(reader);
        cloned.action = clonedAction;
        return cloned;
    }

    protected abstract TriggerEvent<Action> clone(StoryUser reader);

    public abstract Type getType();

    public enum Type {
        AREA, SNEAK, DROP_ITEM
    }
}
