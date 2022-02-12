package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DropItemEvent extends TriggerEvent implements Listener {

    public static final String NAME = "area";

    private static final String ITEM = "item";
    private static final String CLEAR_ITEM = "clear";

    protected final StoryItem item;
    protected final boolean clearItem;

    protected DropItemEvent(StoryItem item, boolean clearItem) {
        super();
        this.item = item;
        this.clearItem = clearItem;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public DropItemEvent(TriggeredAction action, ChapterFile file, String triggerPath) throws ItemNotFoundException {
        super(action);

        int itemId = file.getActionValueInteger(triggerPath, ITEM);
        this.item = StoryServer.getItem(itemId);

        this.clearItem = file.getTriggerValueBoolean(triggerPath, CLEAR_ITEM);
    }

    @Override
    protected TriggerEvent clone(StoryUser reader) {
        return null;
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM;
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

        if (!this.item.getItem().equals(ExItemStack.getItem(e.getItemStack(), false))) {
            return;
        }

        boolean successful = super.triggerAction(((StoryUser) user));

        if (successful && this.clearItem) {
            Server.runTaskLaterSynchrony(() -> e.getItemDrop().remove(), 20, GameStory.getPlugin());
        }
    }
}
