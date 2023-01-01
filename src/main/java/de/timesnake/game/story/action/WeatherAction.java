/*
 * Copyright (C) 2023 timesnake
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
