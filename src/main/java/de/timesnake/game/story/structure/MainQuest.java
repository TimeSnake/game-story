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

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.elements.InvalidQuestException;
import de.timesnake.game.story.user.StoryReader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainQuest extends Quest {

    protected final Map<String, MainQuest> nextMainQuestByName;
    protected final Map<String, OptionalQuest> nextOptionalQuestByName;

    public MainQuest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
                     Map<String, MainQuest> nextMainQuestByName,
                     Map<String, OptionalQuest> nextOptionalQuestByName, StoryAction firstAction) {
        super(chapter, name, reader, startLocation, firstAction);
        this.nextMainQuestByName = nextMainQuestByName;
        this.nextOptionalQuestByName = nextOptionalQuestByName;
    }

    public MainQuest(Toml quest, String name, StoryAction firstAction) {
        super(quest, name, firstAction);
        this.nextMainQuestByName = new HashMap<>();
        this.nextOptionalQuestByName = new HashMap<>();
    }

    @Override
    public MainQuest clone(StoryChapter chapter, StoryReader reader) {
        HashMap<String, MainQuest> clonedNextMainQuests = new HashMap<>();
        HashMap<String, OptionalQuest> clonedNextOptionalQuests = new HashMap<>();

        for (MainQuest quest : this.nextMainQuestByName.values()) {
            clonedNextMainQuests.put(quest.getName(), quest.clone(chapter, reader));
        }

        for (OptionalQuest quest : this.nextOptionalQuestByName.values()) {
            clonedNextOptionalQuests.put(quest.getName(), quest.clone(chapter, reader));
        }

        return new MainQuest(chapter, this.name, reader, this.startLocation, clonedNextMainQuests,
                clonedNextOptionalQuests, this.firstAction);
    }

    @Override
    public void forEachNext(Consumer<Quest> consumer) {
        consumer.accept(this);
        for (MainQuest quest : this.nextMainQuestByName.values()) {
            quest.forEachNext(consumer);
        }
        for (OptionalQuest quest : this.nextOptionalQuestByName.values()) {
            quest.forEachNext(consumer);
        }
    }

    @Override
    public MainQuest nextQuest() {
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
}
