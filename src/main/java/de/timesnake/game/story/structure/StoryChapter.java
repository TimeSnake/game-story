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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import org.bukkit.GameRule;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class StoryChapter {

    private final Integer id;

    private final String name;
    private final String endMessage;

    private final Quest firstQuest;
    private final LinkedHashMap<String, StoryCharacter<?>> characterByName = new LinkedHashMap<>();

    private final Diary diary;

    private final List<Long> playerSizes;

    private final ExWorld world;

    public StoryChapter(Integer id, String name, String endMessage, Diary diary, Quest firstQuest,
                        List<Long> playerSizes, String worldName, Set<StoryCharacter<?>> characters) {
        this.id = id;
        this.name = name;
        this.endMessage = endMessage;
        this.diary = diary;
        this.firstQuest = firstQuest;
        this.firstQuest.setChapter(this);
        this.playerSizes = playerSizes;
        this.world = Server.getWorld(worldName);

        if (this.world == null) {
            Server.printWarning(Plugin.STORY, "World " + worldName + " not exists", "Part " + this.id);
        }

        for (StoryCharacter<?> character : characters) {
            this.characterByName.put(character.getName(), character);
        }

        this.world.setPVP(false);
        this.world.allowBlockPlace(false);
        this.world.allowBlockBreak(false);
        this.world.allowFluidCollect(false);
        this.world.allowFluidPlace(false);
        this.world.allowBlockBurnUp(false);
        this.world.allowBlockIgnite(false);
        this.world.allowFlintAndSteel(false);
        this.world.allowLightUpInteraction(true);
        this.world.allowFireSpread(false);
        this.world.allowEntityExplode(false);
        this.world.allowEntityBlockBreak(false);
        this.world.allowItemFrameRotate(true);
        this.world.setExceptService(true);
        this.world.allowDropPickItem(true);
        this.world.allowPlaceInBlock(true);
        this.world.allowCakeEat(false);
        this.world.setLockedBlockInventories(List.of(Material.DISPENSER, Material.DROPPER, Material.HOPPER));
        this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);


    }

    private StoryChapter(StoryReader reader, Integer id, String name, String endMessage,
                         Diary diary, Quest firstQuest, List<Long> playerSizes, ExWorld world,
                         LinkedHashMap<String, StoryCharacter<?>> characterByName) {
        this.id = id;
        this.name = name;
        this.endMessage = endMessage;
        this.world = world;
        this.diary = diary.clone(reader);
        this.playerSizes = playerSizes;

        for (StoryCharacter<?> character : characterByName.values()) {
            this.characterByName.put(character.getName(), character.clone(reader, this));
        }

        this.firstQuest = firstQuest.clone(this, reader);
    }

    public StoryChapter clone(StoryReader reader) {
        return new StoryChapter(reader, this.id, this.name, this.endMessage, this.diary, this.firstQuest, this.playerSizes,
                Server.getWorldManager().cloneWorld(this.world.getName() + "_" + reader.getId(), this.world), this.characterByName);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public Quest getFirstQuest() {
        return this.firstQuest;
    }

    public Quest getLastQuest() {
        return this.firstQuest.lastQuest();
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

    public List<Long> getPlayerSizes() {
        return playerSizes;
    }

    public ExWorld getWorld() {
        return world;
    }
}
