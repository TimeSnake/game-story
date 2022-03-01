package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.Set;

public class SleepEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements Listener {

    public static final String NAME = "sleep";

    public SleepEvent() {
        super();
        Server.registerListener(this, GameStory.getPlugin());
    }

    public SleepEvent(Action action) {
        super(action);
    }

    @Override
    protected TriggerEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        return new SleepEvent<>();
    }

    @Override
    public Type getType() {
        return Type.SLEEP;
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent e) {
        if (!this.action.isActive()) {
            return;
        }

        StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

        if (!this.action.getReader().equals(user)) {
            return;
        }

        e.setUseBed(Event.Result.ALLOW);

        super.triggerAction(user);
    }
}
