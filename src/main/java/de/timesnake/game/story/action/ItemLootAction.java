package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.AreaEvent;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemLootAction extends LocationAction {

    public static final String NAME = "item_loot";

    private static final double RADIUS = 8;

    private final List<StoryItem> items;

    protected ItemLootAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, StoryCharacter<?> character, List<StoryItem> items) {
        super(id, diaryPage, next, location, character);
        this.items = items;

        if (this.location.getBlock() instanceof InventoryHolder) {
            ((InventoryHolder) this.location.getBlock()).getInventory().addItem(this.items.stream().map(StoryItem::getItem).toArray(value -> new ItemStack[0]));
        }

    }

    public ItemLootAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException, ItemNotFoundException {
        super(id, diaryPage, file, actionPath);

        this.items = new LinkedList<>();
        for (int itemId : file.getIntegerList(ExFile.toPath(actionPath, ITEM))) {
            this.items.add(StoryServer.getItem(itemId));
        }

        this.triggerEvent = new AreaEvent<>(this, file, actionPath, RADIUS);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ItemLootAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(this.reader.getStoryWorld()), this.character != null ? this.character.clone(this.reader, this.listeners) : null, this.items.stream().map(i -> i.clone(this.reader)).collect(Collectors.toList()));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.startNext();
    }
}
