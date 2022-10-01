package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.List;

public class DelayAction extends StoryAction {

    public static final String NAME = "delay";

    private final int delay;

    protected DelayAction(int id, StoryAction next, int delay) {
        super(id, next);
        this.delay = delay;
    }

    public DelayAction(Toml action, int id, List<Integer> diaryPages) {
        super(id, diaryPages);

        this.delay = action.getLong("delay", 0L).intValue();
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new DelayAction(this.id, clonedNext, this.delay);
    }
}
