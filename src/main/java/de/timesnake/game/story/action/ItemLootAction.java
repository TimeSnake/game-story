package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.AreaEvent;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.inventory.Inventory;
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

    protected ItemLootAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, List<StoryItem> items) {
        super(id, next, location, character);
        this.items = items;

        if (this.location.getBlock().getState() instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) this.location.getBlock().getState()).getInventory();

            inv.clear();
            inv.addItem(this.items.stream().map(StoryItem::getItem).toArray(ItemStack[]::new));
        }

    }

    public ItemLootAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException, ItemNotFoundException {
        super(id, diaryPages, file, actionPath);

        this.items = new LinkedList<>();
        for (int itemId : file.getIntegerList(ExFile.toPath(actionPath, ITEM))) {
            this.items.add(StoryServer.getItem(itemId));
        }

        this.triggerEvent = new AreaEvent<>(this, file, actionPath, RADIUS);
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ItemLootAction(this.id, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), this.character != null ? section.getPart().getCharacter(this.character.getId()) : null, this.items.stream().map(i -> i.clone(this.reader)).collect(Collectors.toList()));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.startNext();
    }
}
