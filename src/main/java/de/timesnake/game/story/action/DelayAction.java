package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public class DelayAction extends StoryAction {

    public static final String NAME = "delay";

    private final int delay;

    protected DelayAction(int id, StoryAction next, int delay) {
        super(id, next);
        this.delay = delay;
    }

    public DelayAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) {
        super(id, diaryPages);

        this.delay = file.getInt(ExFile.toPath(actionPath, DELAY));
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new DelayAction(this.id, clonedNext, this.delay);
    }
}
