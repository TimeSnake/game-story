package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.AreaEvent;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemLootAction extends LocationAction {

    public static final String NAME = "item_loot";

    private static final double RADIUS = 8;

    private final List<StoryItem> items;

    protected ItemLootAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                             List<StoryItem> items) {
        super(id, next, location, character);
        this.items = items;

        if (this.location.getBlock().getState() instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) this.location.getBlock().getState()).getInventory();

            inv.clear();
            inv.addItem(this.items.stream().map(StoryItem::getItem).toArray(ItemStack[]::new));
        }

    }

    public ItemLootAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, UnknownLocationException, ItemNotFoundException {
        super(action, id, diaryPages);

        this.items = new LinkedList<>();
        for (String name : action.getList("items", new LinkedList<String>())) {
            this.items.add(StoryServer.getItem(name));
        }

        this.triggerEvent = new AreaEvent<>(this, action, RADIUS);
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemLootAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
                this.items.stream().map(i -> i.clone(this.reader)).collect(Collectors.toList()));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.startNext();
    }
}
