package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public class PlaySoundAction extends TriggeredAction {

    public static final String NAME = "play_sound";

    protected PlaySoundAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    protected PlaySoundAction(int id, StoryAction next) {
        super(id, next);
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return null;
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {

    }
}
