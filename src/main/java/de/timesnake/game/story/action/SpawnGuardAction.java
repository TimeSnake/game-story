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

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownGuardTypeException;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.bukkit.ExPillager;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpawnGuardAction extends LocationAction {

    public static final String NAME = "spawn_guard";

    private final int size;
    private final GuardType type;
    private final Set<Mob> guards = new HashSet<>();

    protected SpawnGuardAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                               int size, GuardType type) {
        super(id, next, location, character);
        this.size = size;
        this.type = type;
    }

    public SpawnGuardAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException, UnknownGuardTypeException {
        super(action, id, diaryPages);

        this.size = action.getLong("size").intValue();
        this.type = GuardType.fromString(action.getString("guard_type"));

        if (this.type == null) {
            throw new UnknownGuardTypeException(action.getString("guard_type"));
        }
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new SpawnGuardAction(this.id, clonedNext, location.clone().setExWorld(reader.getWorld()),
                this.character.clone(reader, chapter), this.size, this.type);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.spawn();
    }

    private void spawn() {
        for (int i = 0; i < this.size; i++) {
            Mob mob = type.create(this.location);
            this.guards.add(mob);
            EntityManager.spawnEntity(mob);
        }
    }

    public enum GuardType {

        PILLAGER() {
            @Override
            public Mob create(Location location) {
                ExPillager pillager = new ExPillager(location.getWorld(), false, false);
                pillager.setPosition(location.getX(), location.getY(), location.getZ());
                return pillager;
            }
        };

        public static GuardType fromString(String name) {
            return GuardType.valueOf(name.toUpperCase());
        }

        public abstract Mob create(Location location);
    }
}
