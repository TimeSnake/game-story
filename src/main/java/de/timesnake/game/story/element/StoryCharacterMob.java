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

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.bukkit.ExPillager;
import de.timesnake.library.entities.entity.bukkit.ExVillager;
import de.timesnake.library.entities.entity.bukkit.ExVindicator;
import de.timesnake.library.entities.entity.bukkit.HumanEntity;
import de.timesnake.library.entities.entity.extension.Mob;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoal;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoalLookAtPlayer;
import de.timesnake.library.entities.pathfinder.custom.ExCustomPathfinderGoalLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoryCharacterMob extends StoryCharacter<Mob> {

    private final Type type;
    protected List<ExPathfinderGoal> walkPathfinders = new ArrayList<>();

    public StoryCharacterMob(String name, String displayName, ExLocation location, Type type) {
        super(name, displayName, location);
        this.type = type;
        this.entity = this.type.initEntity(this.location);
    }

    public StoryCharacterMob(String name, Toml character) throws MissingArgumentException, InvalidArgumentTypeException {
        super(name, character);

        String typeString = character.getString("type");
        if (typeString == null) {
            throw new MissingArgumentException("type");
        }

        this.type = Type.valueOf(typeString.toUpperCase());
        if (this.type == null) {
            throw new InvalidArgumentTypeException("Could not load type '" + typeString + "' of character entity");
        }
    }

    @Override
    public StoryCharacter<Mob> clone(StoryReader reader, StoryChapter chapter) {
        StoryCharacterMob character = new StoryCharacterMob(this.name, this.displayName,
                this.location.clone().setExWorld(chapter.getWorld()), this.type);
        character.reader = reader;
        return character;
    }

    @Override
    public void spawn() {
        Server.runTaskSynchrony(() -> {
            this.entity.setCustomNameVisible(false);
            this.entity.setPersistent(true);
            this.entity.setInvulnerable(true);
            this.entity.setPosition(this.location.getX(), this.location.getY(), this.location.getZ());
            this.entity.setRotation(this.location.getYaw(), this.location.getPitch());
            this.entity.setRemoveWhenFarAway(false);
            EntityManager.spawnEntity(location.getWorld(), this.entity);
        }, GameStory.getPlugin());
    }

    public void setWalkPathfinders(ExPathfinderGoal... walkPathfinders) {

        for (ExPathfinderGoal oldPathfinderGoal : this.walkPathfinders) {
            this.entity.removePathfinderGoal(oldPathfinderGoal);
        }

        this.walkPathfinders.clear();

        for (ExPathfinderGoal newPathfinderGoal : walkPathfinders) {
            this.entity.addPathfinderGoal(newPathfinderGoal.getPriority(), newPathfinderGoal);
        }

        this.walkPathfinders.addAll(Arrays.asList(walkPathfinders));
    }

    @Override
    public void despawn() {
        this.entity.remove();
    }

    @Override
    public boolean isRotateable() {
        return false;
    }

    public enum Type {

        VILLAGER() {
            @Override
            public Mob initEntity(ExLocation location) {
                ExVillager entity = new ExVillager(location.getWorld(), ExVillager.Type.PLAINS, false, false, false);

                entity.addPathfinderGoal(1, new ExPathfinderGoalLookAtPlayer(HumanEntity.class, 8.0f));
                entity.addPathfinderGoal(1, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 1, 16, 0.1));

                return entity;
            }
        },
        PILLAGER() {
            @Override
            public Mob initEntity(ExLocation location) {
                ExPillager entity = new ExPillager(location.getWorld(), false, false);

                entity.addPathfinderGoal(1, new ExPathfinderGoalLookAtPlayer(HumanEntity.class, 8.0f));
                entity.addPathfinderGoal(1, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 1, 16, 0.1));

                return entity;
            }
        },
        VINDICATOR() {
            @Override
            public Mob initEntity(ExLocation location) {
                ExVindicator entity = new ExVindicator(location.getWorld(), false, false);

                entity.addPathfinderGoal(1, new ExPathfinderGoalLookAtPlayer(HumanEntity.class, 8.0f));
                entity.addPathfinderGoal(1, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 1, 16, 0.1));

                return entity;
            }
        };


        public abstract Mob initEntity(ExLocation location);
    }
}
