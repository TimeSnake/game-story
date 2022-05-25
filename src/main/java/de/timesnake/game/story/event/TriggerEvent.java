package de.timesnake.game.story.event;

import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.Set;

public abstract class TriggerEvent<Action extends TriggeredAction> {

    public static final String LOCATION = "location";
    public static final String CHARACTER = "character";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";


    protected Action action;

    protected TriggerEvent() {

    }

    public TriggerEvent(Action action) {
        this.action = action;
    }

    public void triggerAction(StoryUser user) {
        this.action.trigger(this.getType(), user);
    }

    public TriggerEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners,
                                      Action clonedAction) {
        TriggerEvent<Action> cloned = this.clone(section, reader, listeners);
        cloned.action = clonedAction;
        return cloned;
    }

    protected abstract TriggerEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners);

    public abstract Type getType();

    public enum Type {
        AREA,
        SNEAK,
        DROP_ITEM,
        DROP_ITEM_AT,
        START,
        SLEEP,
        CHAT_CODE
    }
}
