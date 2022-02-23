package de.timesnake.game.story.action;

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class PlaySoundAction extends TriggeredAction {

    public static final String NAME = "play_sound";

    protected PlaySoundAction(int id, BaseComponent[] diaryPage) {
        super(id, diaryPage);
    }

    protected PlaySoundAction(int id, BaseComponent[] diaryPage, StoryAction next) {
        super(id, diaryPage, next);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return null;
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {

    }
}
