/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
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

    public LocationAction(StoryBookBuilder bookBuilder, Toml action, int id, List<Integer> diaryPages) throws StoryParseException {
        super(id, diaryPages);

        if (action.contains(LOCATION)) {
            this.character = null;
            this.location = ExLocation.fromList(action.getList(LOCATION));
        } else if (action.contains(CHARACTER)) {
            this.character = bookBuilder.getCharacter(action.getString(CHARACTER));
            this.location = this.character.getLocation();
        } else {
            throw new MissingArgumentException("location");
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
