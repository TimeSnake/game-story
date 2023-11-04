/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import com.google.common.collect.Streams;
import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.InvalidQuestException;
import de.timesnake.game.story.user.StoryReader;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public non-sealed class MainQuest extends Quest {

  protected final Map<String, MainQuest> nextMainQuestByName = new HashMap<>();
  protected final Map<String, OptionalQuest> nextOptionalQuestByName = new HashMap<>();

  public MainQuest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
      Map<String, Supplier<?>> varSupplier, StoryAction firstAction, int lastActionId) {
    super(chapter, name, reader, startLocation, varSupplier, firstAction, lastActionId);
  }

  public MainQuest(StoryBookBuilder bookBuilder, Toml quest, String name)
      throws InvalidArgumentTypeException {
    super(bookBuilder, quest, name);
  }

  @Override
  public MainQuest clone(StoryChapter chapter, StoryReader reader, Map<String, Quest> visited) {
    if (visited.containsKey(this.getName())) {
      return (MainQuest) visited.get(this.getName());
    }

    MainQuest cloned = new MainQuest(chapter, this.name, reader, this.startLocation,
        this.varSupplier, this.firstAction, this.lastActionId);

    visited.put(this.getName(), cloned);

    for (MainQuest quest : this.nextMainQuestByName.values()) {
      cloned.nextMainQuestByName.put(quest.getName(), quest.clone(chapter, reader, visited));
    }

    for (OptionalQuest quest : this.nextOptionalQuestByName.values()) {
      cloned.nextOptionalQuestByName.put(quest.getName(), quest.clone(chapter, reader, visited));
    }

    this.cloneSkipQuests(chapter, reader, cloned, visited);

    return cloned;
  }

  @Override
  public void forEachNext(Consumer<Quest> consumer, Set<Quest> visited) {
    consumer.accept(this);
    visited.add(this);
    for (MainQuest quest : this.nextMainQuestByName.values()) {
      if (!visited.contains(quest)) {
        quest.forEachNext(consumer, visited);
      }
    }
    for (OptionalQuest quest : this.nextOptionalQuestByName.values()) {
      if (!visited.contains(quest)) {
        quest.forEachNext(consumer, visited);
      }
    }
  }

  public MainQuest getNextQuest() {
    if (this.nextMainQuestByName.isEmpty()) {
      return null;
    }

    if (this.nextMainQuestByName.size() == 1) {
      return this.nextMainQuestByName.values().iterator().next();
    } else {
      if (this.selectedQuest != null) {
        return this.nextMainQuestByName.get(this.selectedQuest);
      } else {
        return null;
      }
    }
  }

  public Collection<OptionalQuest> startNextOptionals() {
    for (OptionalQuest optionalQuest : this.nextOptionalQuestByName.values()) {
      optionalQuest.start(false, true);
    }
    return this.nextOptionalQuestByName.values();
  }

  @Override
  public Quest lastQuest() {
    return this.nextMainQuestByName.isEmpty() ? this : this.nextMainQuestByName.values().iterator().next();
  }

  @Override
  public void addNextQuest(Quest quest) {
    if (quest instanceof MainQuest) {
      this.nextMainQuestByName.put(quest.getName(), (MainQuest) quest);
    } else if (quest instanceof OptionalQuest) {
      this.nextOptionalQuestByName.put(quest.getName(), ((OptionalQuest) quest));
    } else {
      throw new InvalidQuestException("unknown quest type");
    }
  }

  @Override
  public List<? extends Quest> getNextQuests() {
    return Streams.concat(this.nextMainQuestByName.values().stream(),
        this.nextOptionalQuestByName.values().stream()).toList();
  }

  public Collection<MainQuest> getNextMainQuests() {
    return this.nextMainQuestByName.values();
  }

  public Collection<OptionalQuest> getNextOptionalQuests() {
    return this.nextOptionalQuestByName.values();
  }
}
