package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.file.ExFile;

import java.util.UUID;

public class DiaryManager {

    private static final String DIRECTORY = "diaries";

    public DiaryManager() {

    }

    public Diary getUserDiary(UUID uuid, Integer chapterId) {
        ExFile file = new ExFile("game-story/" + DIRECTORY, uuid.toString() + "#" + chapterId);

        return new Diary(file);
    }
}
