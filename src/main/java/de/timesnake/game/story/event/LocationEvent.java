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

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.structure.StoryBookBuilder;

public abstract class LocationEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    protected final ExLocation location;
    protected final StoryCharacter<?> character;

    protected LocationEvent(ExLocation location, StoryCharacter<?> character) {
        super();
        this.location = location;
        this.character = character;
    }

    public LocationEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger) throws StoryParseException {
        super(action);

        if (trigger.contains(CHARACTER)) {
            this.character = bookBuilder.getCharacter(trigger.getString(CHARACTER));
        } else {
            this.character = null;
        }

        if (trigger.contains(LOCATION)) {
            this.location = ExLocation.fromList(trigger.getList(LOCATION));
        } else if (trigger.contains(CHARACTER)) {
            this.location = this.character.getLocation();
        } else {
            throw new MissingArgumentException("location");
        }
    }

}
