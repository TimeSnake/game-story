package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class ItemTradeAction extends LocationAction implements Listener {

    public static final String NAME = "item_trade";

    private static final String CHARACTER = "character";
    private static final String ITEM = "item";

    private final StoryCharacter<?> character;
    private final StoryItem item;

    private boolean dropped = false;

    protected ItemTradeAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(id, diaryPage, next, location);
        this.character = character;
        this.item = item;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public ItemTradeAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws ItemNotFoundException, CharacterNotFoundException {
        super(id, diaryPage, false, file, actionPath);

        int charId = file.getActionValueInteger(actionPath, CHARACTER);
        this.character = StoryServer.getCharater(charId);

        int itemId = file.getActionValueInteger(actionPath, ITEM);
        this.item = StoryServer.getItem(itemId);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ItemTradeAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), this.character.clone(reader, listeners), this.item.clone(reader));
    }

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {

        User user = e.getUser();

        if (!user.equals(this.reader)) {
            return;
        }

        if (!this.isActive()) {
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
                this.startNext();
            } else {
                this.dropped = false;
            }
        }, 20, GameStory.getPlugin());
    }
}
