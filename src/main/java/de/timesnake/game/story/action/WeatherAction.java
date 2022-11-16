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
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.WeatherType;

import java.util.List;

public class WeatherAction extends TriggeredAction {

    public static final String NAME = "weather";

    private final WeatherType type;

    protected WeatherAction(int id, StoryAction next, WeatherType type) {
        super(id, next);
        this.type = type;
    }

    public WeatherAction(Toml action, int id, List<Integer> diaryPages) throws StoryParseException {
        super(id, diaryPages);

        String typeString = action.getString("weather");
        if (typeString == null) {
            throw new MissingArgumentException("weather");
        }

        this.type = WeatherType.valueOf(typeString.toUpperCase());
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new WeatherAction(this.id, clonedNext, this.type);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        switch (this.type) {
            case CLEAR -> this.reader.getWorld().setClearWeatherDuration(Integer.MAX_VALUE);
            case DOWNFALL -> this.reader.getWorld().setWeatherDuration(Integer.MAX_VALUE);
        }
        this.startNext();
    }
}
