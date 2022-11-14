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
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

public class AreaEvent<Action extends TriggeredAction> extends LocationEvent<Action> {

    public static final String NAME = "area";

    private static final String RADIUS = "radius";

    protected final double radius;

    protected AreaEvent(ExLocation location, StoryCharacter<?> character, double radius) {
        super(location, character);
        this.radius = radius;
    }

    public AreaEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger) throws StoryParseException {
        super(action, bookBuilder, trigger);

        Double radius;
        try {
            radius = trigger.getDouble(RADIUS);
        } catch (ClassCastException e) {
            radius = trigger.getLong(RADIUS).doubleValue();
        }

        if (radius == null) {
            throw new MissingArgumentException("radius");
        }

        this.radius = radius;

    }

    public AreaEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger, double radius) throws StoryParseException {
        super(action, bookBuilder, trigger);
        this.radius = radius;
    }

    @StoryEvent
    public void onUserMove(AsyncUserMoveEvent e) {
        if (this.action.getReader() == null || (!this.action.getReader().containsUser(((StoryUser) e.getUser()))
                && !this.action.getReader().containsUser((StoryUser) e.getUser())) || !this.action.isActive()) {
            return;
        }

        StoryUser user = ((StoryUser) e.getUser());

        if (!user.getLocation().getWorld().equals(this.location.getWorld())) {
            return;
        }

        if (user.getLocation().distance(this.location) <= this.radius) {
            this.triggerAction(user);
        }
    }

    @Override
    protected AreaEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new AreaEvent<>(this.location.clone().setExWorld(chapter.getWorld()), this.character != null ?
                section.getChapter().getCharacter(this.character.getName()) : null, this.radius);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}
