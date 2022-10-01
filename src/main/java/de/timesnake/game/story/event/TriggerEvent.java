package de.timesnake.game.story.event;

import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

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

    public TriggerEvent<Action> clone(Quest section, StoryReader reader, Action clonedAction, StoryChapter chapter) {
        TriggerEvent<Action> cloned = this.clone(section, reader, chapter);
        cloned.action = clonedAction;
        return cloned;
    }

    protected abstract TriggerEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter);

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
