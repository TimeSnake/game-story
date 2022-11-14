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

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.exception.UnknownGuardTypeException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.bukkit.ExPillager;
import de.timesnake.library.entities.entity.bukkit.ExRavager;
import de.timesnake.library.entities.entity.bukkit.ExVindicator;
import de.timesnake.library.entities.entity.bukkit.HumanEntity;
import de.timesnake.library.entities.entity.extension.Mob;
import de.timesnake.library.entities.entity.extension.Monster;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoalCrossbowAttack;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoalFloat;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoalRandomStrollLand;
import de.timesnake.library.entities.pathfinder.custom.*;
import de.timesnake.library.reflection.wrapper.ExEnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SpawnGuardAction extends LocationAction {

    public static final String NAME = "spawn_guard";

    private final Supplier<Integer> amount;
    private final GuardType type;
    private final Set<Mob> guards = new HashSet<>();

    protected SpawnGuardAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                               Supplier<Integer> amount, GuardType type) {
        super(id, next, location, character);
        this.amount = amount;
        this.type = type;
    }

    public SpawnGuardAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id, List<Integer> diaryPages)
            throws StoryParseException {
        super(bookBuilder, action, id, diaryPages);

        this.amount = quest.parseAdvancedInt(action, "amount");

        this.type = GuardType.fromString(action.getString("guard_type"));

        if (this.type == null) {
            throw new UnknownGuardTypeException(action.getString("guard_type"));
        }
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new SpawnGuardAction(this.id, clonedNext, location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? this.character.clone(reader, chapter) : null, this.amount, this.type);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.spawn();
        this.startNext();
    }

    private void spawn() {
        Server.runTaskSynchrony(() -> {
            for (int i = 0; i < this.amount.get(); i++) {
                Mob mob = type.create(this.location);
                this.guards.add(mob);
                EntityManager.spawnEntity(mob);
            }
        }, GameStory.getPlugin());
    }

    public enum GuardType {

        PILLAGER() {
            @Override
            public Mob create(Location location) {
                ExPillager pillager = new ExPillager(location.getWorld(), false, false);
                pillager.setPosition(location.getX(), location.getY(), location.getZ());

                pillager.setSlot(ExEnumItemSlot.MAIN_HAND, new ItemStack(Material.CROSSBOW));

                pillager.addPathfinderGoal(0, new ExPathfinderGoalFloat());
                pillager.addPathfinderGoal(3, new ExPathfinderGoalCrossbowAttack(1.0, 15.0F));
                pillager.addPathfinderGoal(4, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 0.9, 32, 5));
                pillager.addPathfinderGoal(8, new ExCustomPathfinderGoalRandomStroll(0.6));
                pillager.addPathfinderGoal(9, new ExCustomPathfinderGoalLookAtPlayer(HumanEntity.class));
                pillager.addPathfinderGoal(10, new ExCustomPathfinderGoalLookAtPlayer(Mob.class));

                pillager.addPathfinderGoal(1, new ExCustomPathfinderGoalHurtByTarget(Monster.class));
                pillager.addPathfinderGoal(2, new ExCustomPathfinderGoalNearestAttackableTarget(HumanEntity.class,
                        20.0));

                return pillager;
            }
        },

        VINDICATOR() {
            @Override
            public Mob create(Location location) {
                ExVindicator vindicator = new ExVindicator(location.getWorld(), false, false);
                vindicator.setPosition(location.getX(), location.getY(), location.getZ());
                vindicator.setSlot(ExEnumItemSlot.MAIN_HAND, new ItemStack(Material.IRON_AXE));

                vindicator.addPathfinderGoal(0, new ExPathfinderGoalFloat());
                vindicator.addPathfinderGoal(3, new ExCustomPathfinderGoalMeleeAttackVindicator(1.1));
                vindicator.addPathfinderGoal(4, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 0.9, 32, 5));
                vindicator.addPathfinderGoal(8, new ExCustomPathfinderGoalRandomStroll(0.6));
                vindicator.addPathfinderGoal(9, new ExCustomPathfinderGoalLookAtPlayer(HumanEntity.class));
                vindicator.addPathfinderGoal(10, new ExCustomPathfinderGoalLookAtPlayer(Mob.class));

                vindicator.addPathfinderGoal(1, new ExCustomPathfinderGoalHurtByTarget(Monster.class));
                vindicator.addPathfinderGoal(2, new ExCustomPathfinderGoalNearestAttackableTarget(HumanEntity.class,
                        20.0));

                return vindicator;
            }
        },

        RAVAGER() {
            @Override
            public Mob create(Location location) {
                ExRavager vindicator = new ExRavager(location.getWorld(), false, false);
                vindicator.setPosition(location.getX(), location.getY(), location.getZ());

                vindicator.addPathfinderGoal(0, new ExPathfinderGoalFloat());
                vindicator.addPathfinderGoal(3, new ExCustomPathfinderGoalMeleeAttackRavager(1));
                vindicator.addPathfinderGoal(4, new ExCustomPathfinderGoalLocation(location.getX(), location.getY(),
                        location.getZ(), 0.9, 32, 5));
                vindicator.addPathfinderGoal(5, new ExPathfinderGoalRandomStrollLand(0.4));
                vindicator.addPathfinderGoal(6, new ExCustomPathfinderGoalLookAtPlayer(HumanEntity.class, 6.0F));
                vindicator.addPathfinderGoal(10, new ExCustomPathfinderGoalLookAtPlayer(Mob.class, 8.0F));

                vindicator.addPathfinderGoal(1, new ExCustomPathfinderGoalHurtByTarget(Monster.class));
                vindicator.addPathfinderGoal(2, new ExCustomPathfinderGoalNearestAttackableTarget(HumanEntity.class,
                        20.0));

                return vindicator;
            }
        };

        public static GuardType fromString(String name) {
            return GuardType.valueOf(name.toUpperCase());
        }

        public abstract Mob create(Location location);
    }
}
