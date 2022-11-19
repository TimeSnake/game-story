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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StoryChapter implements Iterable<Quest> {

    private final String name;

    private final String displayName;
    private final String endMessage;
    private final Quest firstQuest;
    private final LinkedHashMap<String, StoryCharacter<?>> characterByName = new LinkedHashMap<>();
    private final Diary diary;
    private final List<Integer> playerSizes;
    private final ExWorld world;
    private final Integer maxDeaths;
    private String previous;
    private String next;
    private StoryBook book;

    public StoryChapter(String name, String displayName, String endMessage, Diary diary, Quest firstQuest,
                        List<Integer> playerSizes, Integer maxDeaths, String worldName, Set<StoryCharacter<?>> characters) {
        this.name = name;
        this.displayName = displayName;
        this.endMessage = endMessage;
        this.diary = diary;
        this.firstQuest = firstQuest;
        this.firstQuest.setChapter(this);
        this.playerSizes = playerSizes;
        this.maxDeaths = maxDeaths;
        this.world = Server.getWorld(worldName);

        if (this.world == null) {
            Server.printWarning(Plugin.STORY, "World " + worldName + " not exists", "Part " + this.name);
            return;
        }

        for (StoryCharacter<?> character : characters) {
            this.characterByName.put(character.getName(), character);
        }

        this.world.setPVP(false);
        this.world.restrict(ExWorld.Restriction.BLOCK_PLACE, true);
        this.world.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
        this.world.restrict(ExWorld.Restriction.FLUID_COLLECT, true);
        this.world.restrict(ExWorld.Restriction.FLUID_PLACE, true);
        this.world.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
        this.world.restrict(ExWorld.Restriction.BLOCK_IGNITE, true);
        this.world.restrict(ExWorld.Restriction.FLINT_AND_STEEL, false);
        this.world.restrict(ExWorld.Restriction.LIGHT_UP_INTERACTION, false);
        this.world.restrict(ExWorld.Restriction.FIRE_SPREAD, true);
        this.world.restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
        this.world.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
        this.world.restrict(ExWorld.Restriction.ITEM_FRAME_ROTATE, false);
        this.world.setExceptService(true);
        this.world.restrict(ExWorld.Restriction.DROP_PICK_ITEM, false);
        this.world.restrict(ExWorld.Restriction.PLACE_IN_BLOCK, false);
        this.world.restrict(ExWorld.Restriction.CAKE_EAT, true);
        this.world.restrict(ExWorld.Restriction.OPEN_INVENTORIES, List.of(Material.DISPENSER, Material.DROPPER, Material.HOPPER));
        this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);


    }

    private StoryChapter(StoryReader reader, String name, String displayName, String endMessage,
                         Diary diary, Quest firstQuest, List<Integer> playerSizes, Integer maxDeaths, ExWorld world,
                         LinkedHashMap<String, StoryCharacter<?>> characterByName) {
        this.name = name;
        this.displayName = displayName;
        this.endMessage = endMessage;
        this.world = world;
        this.diary = diary.clone(reader);
        this.playerSizes = playerSizes;
        this.maxDeaths = maxDeaths;

        for (StoryCharacter<?> character : characterByName.values()) {
            this.characterByName.put(character.getName(), character.clone(reader, this));
        }

        this.firstQuest = firstQuest.clone(this, reader, new HashMap<>());
    }

    public StoryChapter clone(StoryReader reader) {
        return new StoryChapter(reader, this.name, this.displayName, this.endMessage, this.diary, this.firstQuest, this.playerSizes, this.maxDeaths,
                Server.getWorldManager().cloneWorld(this.world.getName() + "_" + reader.getId(), this.world), this.characterByName);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public String getPrevious() {
        return previous;
    }

    protected void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    protected void setNext(String next) {
        this.next = next;
    }

    public StoryBook getBook() {
        return book;
    }

    protected void setBook(StoryBook book) {
        this.book = book;
    }

    public Quest getFirstQuest() {
        return this.firstQuest;
    }

    public Quest getLastQuest() {
        return this.firstQuest.lastQuest();
    }

    public Quest getQuest(String name) {
        for (Quest quest : this) {
            if (quest.getName().equals(name)) {
                return quest;
            }
        }
        return null;
    }

    public Diary getDiary() {
        return diary;
    }

    public void spawnCharacters() {
        Server.runTaskLaterAsynchrony(() -> {
            for (StoryCharacter<?> character : this.characterByName.values()) {
                character.spawn();
            }
        }, 10, GameStory.getPlugin());
    }

    public void despawnCharacters() {
        this.characterByName.values().forEach(StoryCharacter::despawn);
    }

    public StoryCharacter<?> getCharacter(String name) {
        return this.characterByName.get(name);
    }

    public List<Integer> getPlayerSizes() {
        return playerSizes;
    }

    public ExWorld getWorld() {
        return world;
    }

    public Integer getMaxDeaths() {
        return maxDeaths;
    }

    @NotNull
    @Override
    public Iterator<Quest> iterator() {
        return new QuestIterator(this.firstQuest);
    }

    public static class QuestIterator implements Iterator<Quest> {

        private final Stack<Quest> traversal;

        public QuestIterator(Quest first) {
            traversal = new Stack<>();
            traversal.push(first);
        }

        public boolean hasNext() {
            return !traversal.isEmpty();
        }

        public Quest next() {
            if (!hasNext()) throw new NoSuchElementException();

            Quest current = traversal.pop();

            List<? extends Quest> nextQuests = current.getNextQuests();
            Collections.reverse(nextQuests);
            nextQuests.forEach(traversal::push);

            return current;
        }
    }
}
