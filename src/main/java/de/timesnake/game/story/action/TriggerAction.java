package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.user.StoryUser;

import java.util.Set;

public class TriggerAction extends TriggeredAction {

    public static final String NAME = "trigger";

    public TriggerAction(int id, StoryAction next) {
        super(id, null, next);
    }

    public TriggerAction(int id) {
        super(id, null);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new TriggerAction(this.id, clonedNext);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        super.startNext();
    }
}
