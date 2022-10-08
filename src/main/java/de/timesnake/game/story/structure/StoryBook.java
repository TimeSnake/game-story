/*
 * game-story.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

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
