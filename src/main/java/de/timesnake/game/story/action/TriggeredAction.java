package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public abstract class TriggeredAction extends StoryAction {

    protected TriggerEvent<TriggeredAction> triggerEvent;

    protected TriggeredAction(int id, BaseComponent[] diaryPage) {
        super(id, diaryPage);
    }

    protected TriggeredAction(int id, BaseComponent[] diaryPage, StoryAction next) {
        super(id, diaryPage, next);
    }

    public void setTriggerEvent(TriggerEvent<TriggeredAction> triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    @Override
    public TriggeredAction clone(StoryUser reader, Set<StoryUser> listeners) {
        TriggeredAction cloned = (TriggeredAction) super.clone(reader, listeners);
        if (this.triggerEvent != null) {
            cloned.triggerEvent = this.triggerEvent.clone(reader, listeners, cloned);
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
