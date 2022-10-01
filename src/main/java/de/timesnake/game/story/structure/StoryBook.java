package de.timesnake.game.story.structure;

import java.util.Collection;
import java.util.SortedMap;

public class StoryBook {

    private final int id;

    private final String name;

    private final SortedMap<Integer, StoryChapter> chapterById;

    public StoryBook(int id, String name, SortedMap<Integer, StoryChapter> chapterById) {
        this.id = id;
        this.name = name;
        this.chapterById = chapterById;
    }

    public StoryChapter getChapter(int id) {
        return this.chapterById.get(id);
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public StoryChapter nextChapter(StoryChapter chapter) {
        return this.chapterById.get(chapter.getId() + 1);
    }

    public Collection<StoryChapter> getChapters() {
        return this.chapterById.values();
    }

    public StoryChapter getLastChapter() {
        return this.chapterById.get(this.chapterById.lastKey());
    }
}
