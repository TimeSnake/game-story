/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import de.timesnake.game.story.element.AmbientSound;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.exception.CharacterNotFoundException;
import de.timesnake.game.story.exception.ItemNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StoryBook implements Iterable<StoryChapter> {

  private final String id;
  private final String title;

  private final LinkedHashMap<String, StoryChapter> chapterByName;
  private final Map<String, StoryCharacter<?>> characterByName;
  private final Map<String, StoryItem> itemByName;
  private final Set<AmbientSound> ambientSounds;

  public StoryBook(String id, String title, LinkedHashMap<String, StoryChapter> chapterByName,
                   Map<String, StoryCharacter<?>> characterByName, Map<String, StoryItem> itemByName,
                   Set<AmbientSound> ambientSounds) {
    this.id = id;
    this.title = title;
    this.chapterByName = chapterByName;
    this.characterByName = characterByName;
    this.itemByName = itemByName;
    this.ambientSounds = ambientSounds;

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

  public String getId() {
    return this.id;
  }

  public String getTitle() {
    return title;
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
