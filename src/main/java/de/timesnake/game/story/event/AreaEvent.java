package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class AreaEvent<Action extends TriggeredAction> extends LocationEvent<Action> implements Listener {

    public static final String NAME = "area";

    private static final String RADIUS = "radius";

    protected final double radius;

    protected AreaEvent(ExLocation location, StoryCharacter<?> character, double radius) {
        super(location, character);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public AreaEvent(Action action, ChapterFile file, String triggerPath) throws CharacterNotFoundException, UnknownLocationException {
        super(action, file, triggerPath);

        this.radius = file.getDouble(ExFile.toPath(triggerPath, RADIUS));
    }

    public AreaEvent(Action action, ChapterFile file, String triggerPath, double radius) throws CharacterNotFoundException, UnknownLocationException {
        super(action, file, triggerPath);
        this.radius = radius;
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {
        if (this.action.getReader() == null || (!this.action.getListeners().contains(((StoryUser) e.getUser())) && !this.action.getReader().equals(e.getUser())) || !this.action.isActive()) {
            return;
        }

        StoryUser user = ((StoryUser) e.getUser());

        if (!user.getLocation().getWorld().equals(this.location.getWorld())) {
            return;
        }

        if (user.getLocation().distance(this.location) <= this.radius) {
            this.triggerAction(user);
        }
    }

    @Override
    protected AreaEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        return new AreaEvent<>(this.location.clone().setExWorld(reader.getStoryWorld()), this.character != null ? section.getPart().getCharacter(this.character.getId()) : null, this.radius);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}
