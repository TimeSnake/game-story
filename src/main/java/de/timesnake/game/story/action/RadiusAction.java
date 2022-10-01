package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class RadiusAction extends LocationAction implements Listener {

    protected final Double radius;

    public RadiusAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, Double radius) {
        super(id, next, location, character);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public RadiusAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException {
        super(action, id, diaryPages);
        double radius;
        try {
            radius = action.getDouble(RADIUS);
        } catch (ClassCastException e) {
            radius = action.getLong(RADIUS).doubleValue();
        }
        this.radius = radius;
    }

    public RadiusAction(Toml action, int id, List<Integer> diaryPages, Double radius)
            throws CharacterNotFoundException, UnknownLocationException {
        super(action, id, diaryPages);
        this.radius = radius;
    }

}
