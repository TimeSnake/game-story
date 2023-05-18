/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.InvalidQuestException;
import de.timesnake.game.story.user.StoryReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public non-sealed class OptionalQuest extends Quest {

  private final Map<String, OptionalQuest> nextQuestByName = new HashMap<>();

  public OptionalQuest(StoryChapter chapter, String name, StoryReader reader,
      ExLocation startLocation,
      Map<String, Supplier<?>> varSupplier, StoryAction firstAction, int lastActionId) {
    super(chapter, name, reader, startLocation, varSupplier, firstAction, lastActionId);
  }

  public OptionalQuest(StoryBookBuilder bookBuilder, Toml quest, String name)
      throws InvalidArgumentTypeException {
    super(bookBuilder, quest, name);
  }

  @Override
  public OptionalQuest clone(StoryChapter chapter, StoryReader reader, Map<String, Quest> visited) {
    OptionalQuest cloned = new OptionalQuest(chapter, this.name, reader, this.startLocation,
        this.varSupplier, this.firstAction, this.lastActionId);

    visited.put(this.getName(), cloned);

    for (OptionalQuest quest : this.nextQuestByName.values()) {
      OptionalQuest next =
          visited.containsKey(quest.getName()) ? (OptionalQuest) visited.get(quest.getName())
              : quest.clone(chapter, reader, visited);
      cloned.nextQuestByName.put(quest.getName(), next);
    }

    this.cloneSkipQuests(chapter, reader, cloned, visited);

    return cloned;
  }

  @Override
  public void forEachNext(Consumer<Quest> consumer, Set<Quest> visited) {
    consumer.accept(this);
    visited.add(this);
    for (OptionalQuest quest : this.nextQuestByName.values()) {
      if (!visited.contains(quest)) {
        quest.forEachNext(consumer, visited);
      }
    }
  }

  @Override
  public Quest nextQuest() {
    for (OptionalQuest optionalQuest : this.nextQuestByName.values()) {
      optionalQuest.start(false, true);
    }
    return null;
  }

  @Override
  public Quest lastQuest() {
    return this.nextQuestByName.size() == 0 ? this
        : this.nextQuestByName.values().iterator().next();
  }

  @Override
  public void addNextQuest(Quest quest) {
    if (quest instanceof OptionalQuest) {
      this.nextQuestByName.put(quest.getName(), ((OptionalQuest) quest));
    } else if (quest instanceof MainQuest) {
      throw new InvalidQuestException("main-quest can not be a successor of a optional quest");
    } else {
      throw new InvalidQuestException("unknown quest type");
    }
  }

  @Override
  public List<OptionalQuest> getNextQuests() {
    return new ArrayList<>(this.nextQuestByName.values());
  }
}
