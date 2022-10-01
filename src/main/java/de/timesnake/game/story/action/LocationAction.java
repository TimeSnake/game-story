package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.server.StoryServer;

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

    public LocationAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPages);

        if (action.contains(LOCATION)) {
            this.character = null;
            this.location = ExLocation.fromList(action.getList(LOCATION));
        } else if (action.contains(CHARACTER)) {
            this.character = StoryServer.getCharater(action.getString(CHARACTER));
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
