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
import java.util.function.Consumer;
import java.util.function.Supplier;

public non-sealed class OptionalQuest extends Quest {

    private final Map<String, OptionalQuest> nextQuestByName;

    public OptionalQuest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
                         Map<String, Supplier<?>> varSupplier,
                         Map<String, OptionalQuest> nextQuestByName, StoryAction firstAction) {
        super(chapter, name, reader, startLocation, varSupplier, firstAction);
        this.nextQuestByName = nextQuestByName;
    }

    public OptionalQuest(StoryBookBuilder bookBuilder, Toml quest, String name) throws InvalidArgumentTypeException {
        super(bookBuilder, quest, name);
        this.nextQuestByName = new HashMap<>();
    }

    @Override
    public OptionalQuest clone(StoryChapter chapter, StoryReader reader) {
        Map<String, OptionalQuest> clonedNextQuests = new HashMap<>();
        for (OptionalQuest quest : this.nextQuestByName.values()) {
            clonedNextQuests.put(quest.getName(), quest.clone(chapter, reader));
        }
        return new OptionalQuest(chapter, this.name, reader, this.startLocation,
                this.varSupplier, clonedNextQuests, this.firstAction);
    }

    @Override
    public void forEachNext(Consumer<Quest> consumer) {
        consumer.accept(this);
        for (OptionalQuest quest : this.nextQuestByName.values()) {
            quest.forEachNext(consumer);
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
        return this.nextQuestByName.size() == 0 ? this : this.nextQuestByName.values().iterator().next();
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
