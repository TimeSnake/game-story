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

    protected boolean finished = false;

    public MainQuest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
                     Map<String, Supplier<?>> varSupplier, StoryAction firstAction, int lastActionId) {
        super(chapter, name, reader, startLocation, varSupplier, firstAction, lastActionId);
    }

    public MainQuest(StoryBookBuilder bookBuilder, Toml quest, String name) throws InvalidArgumentTypeException {
        super(bookBuilder, quest, name);
    }

    @Override
    public MainQuest clone(StoryChapter chapter, StoryReader reader, Map<String, Quest> visited) {
        MainQuest cloned = new MainQuest(chapter, this.name, reader, this.startLocation, this.varSupplier, this.firstAction, this.lastActionId);

        visited.put(this.getName(), cloned);

        for (MainQuest quest : this.nextMainQuestByName.values()) {
            MainQuest next = visited.containsKey(quest.getName()) ? (MainQuest) visited.get(quest.getName()) : quest.clone(chapter, reader, visited);
            cloned.nextMainQuestByName.put(quest.getName(), next);
        }

        for (OptionalQuest quest : this.nextOptionalQuestByName.values()) {
            OptionalQuest next = visited.containsKey(quest.getName()) ? (OptionalQuest) visited.get(quest.getName()) : quest.clone(chapter, reader, visited);
            cloned.nextOptionalQuestByName.put(quest.getName(), next);
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

    @Override
    public MainQuest nextQuest() {
        this.finished = true;

        if (this.nextMainQuestByName.size() == 0) {
            return null;
        }

        for (OptionalQuest optionalQuest : this.nextOptionalQuestByName.values()) {
            optionalQuest.start(false, true);
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

    @Override
    public Quest lastQuest() {
        return this.nextMainQuestByName.size() == 0 ? this : this.nextMainQuestByName.values().iterator().next();
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

    @Override
    public void start(boolean teleport, boolean spawnEntities) {
        if (!this.finished) {
            super.start(teleport, spawnEntities);
        }
    }

    public Collection<MainQuest> getNextMainQuests() {
        return this.nextMainQuestByName.values();
    }

    public Collection<OptionalQuest> getNextOptionalQuests() {
        return this.nextOptionalQuestByName.values();
    }
}
