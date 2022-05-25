package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;

public abstract class LocationEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    protected final ExLocation location;
    protected final StoryCharacter<?> character;

    protected LocationEvent(ExLocation location, StoryCharacter<?> character) {
        super();
        this.location = location;
        this.character = character;
    }

    public LocationEvent(Action action, ChapterFile file, String triggerPath) throws CharacterNotFoundException,
            UnknownLocationException {
        super(action);

        if (file.contains(triggerPath + "." + CHARACTER)) {
            this.character = StoryServer.getCharater(file.getInt(ExFile.toPath(triggerPath, CHARACTER)));
        } else {
            this.character = null;
        }

        if (file.contains(triggerPath + "." + LOCATION)) {
            this.location = new ExLocation(null, file.getDoubleTriple(ExFile.toPath(triggerPath, LOCATION), X, Y, Z));
        } else if (file.contains(triggerPath + "." + CHARACTER)) {
            this.location = this.character.getLocation();
        } else {
            throw new UnknownLocationException();
        }
    }

}
