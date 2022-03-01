package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class DropItemEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements Listener {

    public static final String NAME = "drop";

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

    public DropItemEvent(Action action, ChapterFile file, String triggerPath) throws ItemNotFoundException {
        super(action);

        int itemId = file.getInt(ExFile.toPath(triggerPath, ITEM));
        this.item = StoryServer.getItem(itemId);

        this.clearItem = file.getBoolean(ExFile.toPath(triggerPath, CLEAR_ITEM));
    }

    @Override
    protected DropItemEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        return new DropItemEvent<>(this.item.clone(reader), this.clearItem);
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

        super.triggerAction(((StoryUser) user));

        if (this.clearItem) {
            Server.runTaskLaterSynchrony(() -> e.getItemDrop().remove(), 20, GameStory.getPlugin());
        }
    }
}
