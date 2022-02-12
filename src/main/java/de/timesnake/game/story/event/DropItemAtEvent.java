package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.ItemAction;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DropItemAtEvent<Action extends TriggeredAction & ItemAction> extends LocationEvent<Action> implements Listener {

    private static final String NAME = "drop_at";

    private boolean dropped = false;

    protected DropItemAtEvent(StoryItem item, boolean clearItem) {
        super(item, clearItem);
    }

    public DropItemAtEvent(Action action, ChapterFile file, String triggerPath) throws ItemNotFoundException {
        super(action, file, triggerPath);
    }

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {

        User user = e.getUser();

        if (!user.equals(this.action.getReader())) {
            return;
        }

        if (!this.action.isActive()) {
            return;
        }

        if (!this.action.getItem().getItem().equals(ExItemStack.getItem(e.getItemStack(), false))) {
            return;
        }

        if (!this.location.getExWorld().equals(user.getExWorld())) {
            return;
        }

        if (this.dropped) {
            return;
        }

        this.dropped = true;

        Server.runTaskLaterSynchrony(() -> {
            if (this.location.distanceSquared(e.getItemDrop().getLocation()) < 2) {
                e.getItemDrop().remove();
                this.startNext();
            } else {
                this.dropped = false;
            }
        }, 20, GameStory.getPlugin());
    }

    @Override
    protected TriggerEvent<Action> clone(StoryUser reader) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }
}
