/*
 * timesnake.game-story.main
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
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.CharacterNotFoundException;
import de.timesnake.game.story.exception.UnknownLocationException;
import de.timesnake.game.story.structure.StoryBookBuilder;

import java.util.Collection;
import java.util.List;

public abstract class LocationAction extends TriggeredAction {

    protected final ExLocation location;
    protected final StoryCharacter<?> character;

    protected LocationAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character) {
        super(id, next);
        this.location = location;
        this.character = character;
    }

    public LocationAction(StoryBookBuilder bookBuilder, Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPages);

        if (action.contains(LOCATION)) {
            this.character = null;
            this.location = ExLocation.fromList(action.getList(LOCATION));
        } else if (action.contains(CHARACTER)) {
            this.character = bookBuilder.getCharacter(action.getString(CHARACTER));
            this.location = this.character.getLocation();
        } else {
            throw new UnknownLocationException();
        }
    }

    @Override
    public Collection<String> getCharacterNames() {
        Collection<String> names = super.getCharacterNames();

        if (this.character != null) {
            names.add(this.character.getName());
        }
        return names;
    }
}
