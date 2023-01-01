/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.event.AreaEvent;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.*;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ItemLootAction extends LocationAction {

    public static final String NAME = "item_loot";

    private static final double RADIUS = 8;

    private final List<StoryItem> storyItems;
    private final List<ItemStack> items;

    private final Order order;

    protected ItemLootAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                             List<StoryItem> storyItems, List<ItemStack> items, Order order) {
        super(id, next, location, character);
        this.storyItems = storyItems;
        this.items = items;
        this.order = order;
    }

    public ItemLootAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id, List<Integer> diaryPages)
            throws StoryParseException {
        super(bookBuilder, action, id, diaryPages);

        this.storyItems = new LinkedList<>();
        this.items = new LinkedList<>();

        if (action.contains("items")) {
            for (Object name : action.getList("items")) {
                try {
                    this.storyItems.add(bookBuilder.getItem(((String) name)));
                } catch (ItemNotFoundException e) {
                    Material material = Material.getMaterial(((String) name).toUpperCase());
                    if (material != null) {
                        this.items.add(new ItemStack(material));
                    } else {
                        throw new InvalidArgumentTypeException("'" + name + "' is not a story item nor a material");
                    }
                }
            }
        } else {
            throw new MissingArgumentException("items");
        }

        String orderString = action.getString("order");

        if (orderString != null) {
            this.order = Order.valueOf(orderString.toUpperCase());
        } else {
            this.order = Order.RANDOM;
        }

        if (this.order == null) {
            throw new InvalidArgumentTypeException("Invalid item order '" + orderString + "'");
        }

        this.triggerEvent = new AreaEvent<>(this, bookBuilder, action, RADIUS);
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemLootAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
                this.storyItems.stream().map(i -> i.clone(this.reader)).collect(Collectors.toList()), this.items, this.order);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (!this.isActive()) {
            return;
        }

        if (this.location.getBlock().getState() instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) this.location.getBlock().getState()).getInventory();

            switch (this.order) {
                case ORDERED -> {
                    inv.addItem(this.storyItems.stream().map(StoryItem::getItem).toArray(ItemStack[]::new));
                    inv.addItem(this.items.toArray(ItemStack[]::new));
                }
                case RANDOM -> {
                    Random random = new Random();
                    int slot = random.nextInt(inv.getSize());
                    for (StoryItem item : this.storyItems) {
                        while (inv.getItem(slot) != null) {
                            slot = random.nextInt(inv.getSize());
                        }
                        inv.setItem(slot, item.getItem());
                    }
                    for (ItemStack item : this.items) {
                        while (inv.getItem(slot) != null) {
                            slot = random.nextInt(inv.getSize());
                        }
                        inv.setItem(slot, item);
                    }
                }
            }
        } else {
            throw new StoryGamePlayException("Could not set loot item, block has no inventory");
        }
        this.startNext();
    }

    private enum Order {
        ORDERED,
        RANDOM
    }

}
