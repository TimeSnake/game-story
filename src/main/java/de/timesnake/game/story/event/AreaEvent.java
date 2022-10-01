package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AreaEvent<Action extends TriggeredAction> extends LocationEvent<Action> implements Listener {

    public static final String NAME = "area";

    private static final String RADIUS = "radius";

    protected final double radius;

    protected AreaEvent(ExLocation location, StoryCharacter<?> character, double radius) {
        super(location, character);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public AreaEvent(Action action, Toml trigger) throws CharacterNotFoundException,
            UnknownLocationException {
        super(action, trigger);

        double radius;
        try {
            radius = trigger.getDouble(RADIUS);
        } catch (ClassCastException e) {
            radius = trigger.getLong(RADIUS).doubleValue();
        }
        this.radius = radius;

    }

    public AreaEvent(Action action, Toml trigger, double radius) throws CharacterNotFoundException, UnknownLocationException {
        super(action, trigger);
        this.radius = radius;
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {
        if (this.action.getReader() == null || (!this.action.getReader().containsUser(((StoryUser) e.getUser()))
                && !this.action.getReader().containsUser((StoryUser) e.getUser())) || !this.action.isActive()) {
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
    protected AreaEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new AreaEvent<>(this.location.clone().setExWorld(chapter.getWorld()), this.character != null ?
                section.getChapter().getCharacter(this.character.getName()) : null, this.radius);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}
