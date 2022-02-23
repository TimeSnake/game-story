package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class DelayAction extends StoryAction {

    public static final String NAME = "delay";

    private final int delay;

    protected DelayAction(int id, BaseComponent[] diaryPage, StoryAction next, int delay) {
        super(id, diaryPage, next);
        this.delay = delay;
    }

    public DelayAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) {
        super(id, diaryPage);

        this.delay = file.getInt(ExFile.toPath(actionPath, DELAY));
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new DelayAction(this.id, this.diaryPage, clonedNext, this.delay);
    }
}
