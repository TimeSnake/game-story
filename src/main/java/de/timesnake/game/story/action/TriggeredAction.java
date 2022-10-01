package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;

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
    public TriggeredAction clone(Quest quest, StoryReader reader, StoryChapter chapter) {
        TriggeredAction cloned = (TriggeredAction) super.clone(quest, reader, chapter);
        if (this.triggerEvent != null) {
            cloned.triggerEvent = this.triggerEvent.clone(quest, reader, cloned, chapter);
        }
        return cloned;
    }

    @Override
    public void start() {
        super.start();

        if (this.triggerEvent == null) {
            this.trigger(TriggerEvent.Type.START, this.reader.anyUser());
        }
    }

    public abstract void trigger(TriggerEvent.Type type, StoryUser user);

}
