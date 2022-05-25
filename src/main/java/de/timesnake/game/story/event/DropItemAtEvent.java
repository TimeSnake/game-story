package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class DropItemAtEvent<Action extends TriggeredAction> extends LocationEvent<Action> implements Listener {

    public static final String NAME = "drop_at";

    private static final String ITEM = "item";


    private final StoryItem item;

    private boolean dropped = false;

    protected DropItemAtEvent(ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(location, character);

        this.item = item;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public DropItemAtEvent(Action action, ChapterFile file, String triggerPath) throws ItemNotFoundException,
            CharacterNotFoundException, UnknownLocationException {
        super(action, file, triggerPath);

        int itemId = file.getInt(ExFile.toPath(triggerPath, ITEM));
        this.item = StoryServer.getItem(itemId);
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
                this.triggerAction(((StoryUser) user));
            } else {
                this.dropped = false;
            }
        }, 20, GameStory.getPlugin());
    }

    @Override
    protected DropItemAtEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        return new DropItemAtEvent<>(this.location.clone().setExWorld(reader.getStoryWorld()),
                this.character != null ? section.getPart().getCharacter(this.character.getId()) : null,
                this.item.clone(reader));
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM_AT;
    }
}
