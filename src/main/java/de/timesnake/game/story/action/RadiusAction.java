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
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.MissingArgumentException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class RadiusAction extends LocationAction implements Listener {

    protected final Double radius;

    public RadiusAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, Double radius) {
        super(id, next, location, character);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public RadiusAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException, MissingArgumentException {
        super(action, id, diaryPages);
        Double radius;
        try {
            radius = action.getDouble(RADIUS);
        } catch (ClassCastException e) {
            radius = action.getLong(RADIUS).doubleValue();
        }

        if (radius == null) {
            throw new MissingArgumentException("radius");
        }


        this.radius = radius;
    }

    public RadiusAction(Toml action, int id, List<Integer> diaryPages, Double radius)
            throws CharacterNotFoundException, UnknownLocationException {
        super(action, id, diaryPages);
        this.radius = radius;
    }

}
