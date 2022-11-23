/*
 * workspace.game-story.main
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

import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.exception.CharacterNotFoundException;
import de.timesnake.game.story.exception.ItemNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class StoryBook implements Iterable<StoryChapter> {

    private final int id;

    private final String name;

    private final LinkedHashMap<String, StoryChapter> chapterByName;
    private final Map<String, StoryCharacter<?>> characterByName;
    private final Map<String, StoryItem> itemByName;

    public StoryBook(int id, String name, LinkedHashMap<String, StoryChapter> chapterByName,
                     Map<String, StoryCharacter<?>> characterByName, Map<String, StoryItem> itemByName) {
        this.id = id;
        this.name = name;
        this.chapterByName = chapterByName;
        this.characterByName = characterByName;
        this.itemByName = itemByName;

        for (StoryChapter chapter : this.chapterByName.values()) {
            chapter.setBook(this);
        }
    }

    public StoryChapter getChapter(String name) {
        return this.chapterByName.get(name);
    }

    public StoryChapter getPreviousChapter(String name) {
        return this.chapterByName.get(this.chapterByName.get(name).getPrevious());
    }

    public StoryChapter getNextChapter(String name) {
        return this.chapterByName.get(this.chapterByName.get(name).getNext());
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public Collection<StoryChapter> getChapters() {
        return this.chapterByName.values();
    }

    public StoryChapter getFirstChapter() {
        for (StoryChapter chapter : this.chapterByName.values()) {
            if (chapter.getPrevious() == null) {
                return chapter;
            }
        }
        return null;
    }

    public StoryChapter getLastChapter() {
        for (StoryChapter chapter : this.chapterByName.values()) {
            if (chapter.getNext() == null) {
                return chapter;
            }
        }
        return null;
    }

    public StoryCharacter<?> getCharacter(String name) throws CharacterNotFoundException {
        StoryCharacter<?> character = this.characterByName.get(name);
        if (character == null) {
            throw new CharacterNotFoundException(name);
        }

        return character;
    }

    public StoryItem getItem(String name) throws ItemNotFoundException {
        StoryItem item = this.itemByName.get(name);
        if (item == null) {
            throw new ItemNotFoundException(name);
        }

        return item;
    }

    @NotNull
    @Override
    public Iterator<StoryChapter> iterator() {
        return this.chapterByName.values().iterator();
    }
}
