package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class LocationAction extends TriggeredAction {

    protected final ExLocation location;
    protected final StoryCharacter<?> character;

    protected LocationAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, StoryCharacter<?> character) {
        super(id, diaryPage, next);
        this.location = location;
        this.character = character;
    }

    public LocationAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPage);

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
    public void spawnEntities() {
        if (this.character != null) {
            this.character.spawn();
        }

        super.spawnEntities();
    }

    @Override
    public void despawnEntities() {
        if (this.character != null) {
            this.character.despawn();
        }

        super.despawnEntities();
    }
}
