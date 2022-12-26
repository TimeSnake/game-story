/*
 * Copyright (C) 2022 timesnake
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

        if (trigger.contains("character")) {
            this.character = bookBuilder.getCharacter(trigger.getString("character"));
        } else {
            this.character = null;
        }

        if (trigger.contains("trigger_location")) {
            this.location = ExLocation.fromList(trigger.getList("trigger_location"));
        } else if (trigger.contains("location")) {
            this.location = ExLocation.fromList(trigger.getList("location"));
        } else if (trigger.contains("character")) {
            this.location = this.character.getLocation();
        } else {
            throw new MissingArgumentException("location");
        }
    }

}
