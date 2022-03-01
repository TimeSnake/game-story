package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public class TriggerAction extends TriggeredAction {

    public static final String NAME = "trigger";

    public TriggerAction(int id, StoryAction next) {
        super(id, next);
    }

    public TriggerAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new TriggerAction(this.id, clonedNext);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        super.startNext();
    }
}
