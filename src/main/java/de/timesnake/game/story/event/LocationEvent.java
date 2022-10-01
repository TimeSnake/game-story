package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.server.StoryServer;

public abstract class LocationEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    protected final ExLocation location;
    protected final StoryCharacter<?> character;

    protected LocationEvent(ExLocation location, StoryCharacter<?> character) {
        super();
        this.location = location;
        this.character = character;
    }

    public LocationEvent(Action action, Toml trigger) throws CharacterNotFoundException,
            UnknownLocationException {
        super(action);

        if (trigger.contains(CHARACTER)) {
            this.character = StoryServer.getCharater(trigger.getString(CHARACTER));
        } else {
            this.character = null;
        }

        if (trigger.contains(LOCATION)) {
            this.location = ExLocation.fromList(trigger.getList(LOCATION));
        } else if (trigger.contains(CHARACTER)) {
            this.location = this.character.getLocation();
        } else {
            throw new UnknownLocationException();
        }
    }

}
