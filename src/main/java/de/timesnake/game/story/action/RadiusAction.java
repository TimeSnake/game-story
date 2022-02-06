package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class RadiusAction extends LocationAction implements Listener {

    private static final String RADIUS = "radius";

    protected final Double radius;

    public RadiusAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, Double radius) {
        super(id, diaryPage, next, location);
        this.radius = radius;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public RadiusAction(int id, BaseComponent[] diaryPage, boolean yawPitch, ChapterFile file, String actionPath) {
        super(id, diaryPage, yawPitch, file, actionPath);
        this.radius = file.getActionValueDouble(actionPath, RADIUS);
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {
        if (this.reader == null || (!this.listeners.contains(((StoryUser) e.getUser())) && !this.reader.equals(e.getUser())) || !this.isActive()) {
            return;
        }

        StoryUser user = ((StoryUser) e.getUser());

        if (!user.getLocation().getWorld().equals(this.location.getWorld())) {
            return;
        }

        if (user.getLocation().distance(this.location) <= this.radius) {
            this.onUserNearby(user);
        }
    }

    protected abstract void onUserNearby(StoryUser user);

}
