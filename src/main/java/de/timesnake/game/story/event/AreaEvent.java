package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AreaEvent extends LocationEvent implements Listener {

    public static final String NAME = "area";

    private static final String RADIUS = "radius";

    protected final double radius;

    protected AreaEvent(ExLocation location, double radius) {
        super(location);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public AreaEvent(TriggeredAction action, ChapterFile file, String triggerPath) {
        super(action, file, triggerPath);

        this.radius = file.getTriggerValueDouble(triggerPath, RADIUS);
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
    protected TriggerEvent clone(StoryUser reader) {
        return new AreaEvent(this.location.clone().setExWorld(reader.getStoryWorld()), this.radius);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}