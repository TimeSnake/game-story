package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.Listener;

public abstract class RadiusAction extends LocationAction implements Listener {

    protected final Double radius;

    public RadiusAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, StoryCharacter<?> character, Double radius) {
        super(id, diaryPage, next, location, character);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public RadiusAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPage, file, actionPath);
        this.radius = file.getDouble(ExFile.toPath(actionPath, RADIUS));
    }

    public RadiusAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath, Double radius) throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPage, file, actionPath);
        this.radius = radius;
    }

}
