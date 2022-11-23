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
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import org.bukkit.entity.LivingEntity;

public abstract class StoryCharacter<Entity extends de.timesnake.library.entities.entity.extension.LivingEntity> {

    public static StoryCharacter<?> initCharacter(String name, Toml character) throws MissingArgumentException, InvalidArgumentTypeException {
        String type = character.getString(TYPE);
        if (type == null) {
            return null;
        }

        return switch (type.toLowerCase()) {
            case StoryCharacterPlayer.NAME -> new StoryCharacterPlayer(name, character);
            default -> new StoryCharacterMob(name, character);
        };

    }

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String LOCATION = "location";

    protected final String name;
    protected final String displayName;
    protected final ExLocation location;
    protected Entity entity;
    protected StoryReader reader;

    public StoryCharacter(String name, String displayName, ExLocation location) {
        this.name = name;
        this.displayName = displayName;
        this.location = location;
    }

    public StoryCharacter(String name, Toml character) {
        this.name = name;
        this.displayName = character.getString(NAME);
        this.location = ExLocation.fromList(character.getList(LOCATION));
        this.entity = null;
    }

    public abstract StoryCharacter<Entity> clone(StoryReader reader, StoryChapter chapter);

    public String getDisplayName() {
        return displayName;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ExLocation getLocation() {
        return location;
    }

    public abstract void spawn();

    public abstract void despawn();

    public abstract boolean isRotateable();

    public String getName() {
        return this.name;
    }

}
