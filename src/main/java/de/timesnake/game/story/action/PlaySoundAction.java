package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;

public class PlaySoundAction extends TriggeredAction {

    public static final String NAME = "play_sound";

    protected PlaySoundAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    protected PlaySoundAction(int id, StoryAction next) {
        super(id, next);
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return null;
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {

    }
}
