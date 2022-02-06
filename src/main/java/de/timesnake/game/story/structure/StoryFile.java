package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.file.ExFile;

import java.util.List;

public class StoryFile extends ExFile {

    private static final String CHAPTERS = "chapters";

    public StoryFile() {
        super("game-story", "story");
    }

    public List<Integer> getChapterIds() {
        return super.getIntegerList(CHAPTERS);
    }
}
