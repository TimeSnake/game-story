package de.timesnake.game.story.action;

import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class DelayAction extends StoryAction {

    private static final String DELAY = "delay";

    private final int delay;

    protected DelayAction(int id, BaseComponent[] diaryPage, StoryAction next, int delay) {
        super(id, diaryPage, next);
        this.delay = delay;
    }

    protected DelayAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) {
        super(id, diaryPage);

        this.delay = file.getActionValueInteger(actionPath, DELAY);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new DelayAction(this.id, this.diaryPage, clonedNext, this.delay);
    }
}
