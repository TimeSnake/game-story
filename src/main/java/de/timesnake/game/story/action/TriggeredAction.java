package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public abstract class TriggeredAction extends StoryAction {

    protected TriggerEvent<TriggeredAction> triggerEvent;

    protected TriggeredAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    protected TriggeredAction(int id, StoryAction next) {
        super(id, next);
    }

    public void setTriggerEvent(TriggerEvent<TriggeredAction> triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    @Override
    public TriggeredAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        TriggeredAction cloned = (TriggeredAction) super.clone(section, reader, listeners);
        if (this.triggerEvent != null) {
            cloned.triggerEvent = this.triggerEvent.clone(section, reader, listeners, cloned);
        }
        return cloned;
    }

    @Override
    public void start() {
        super.start();

        if (this.triggerEvent == null) {
            this.trigger(TriggerEvent.Type.START, this.reader);
        }
    }

    public abstract void trigger(TriggerEvent.Type type, StoryUser user);

}
