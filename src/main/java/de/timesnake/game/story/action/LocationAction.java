package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;

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

    public LocationAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPages);

        if (file.contains(ExFile.toPath(actionPath, LOCATION))) {
            this.character = null;
            this.location = new ExLocation(null, file.getDoubleTriple(ExFile.toPath(actionPath, LOCATION), X, Y, Z));
        } else if (file.contains(ExFile.toPath(actionPath, CHARACTER))) {
            this.character = StoryServer.getCharater(file.getInt(ExFile.toPath(actionPath, CHARACTER)));
            this.location = this.character.getLocation();
        } else {
            throw new UnknownLocationException();
        }
    }

    @Override
    public Collection<Integer> getCharacterIds() {
        Collection<Integer> ids = super.getCharacterIds();

        if (this.character != null) {
            ids.add(this.character.getId());
        }
        return ids;
    }
}
