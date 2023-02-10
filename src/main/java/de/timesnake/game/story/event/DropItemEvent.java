/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.ItemNotFoundException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;

import java.util.function.Supplier;

public class DropItemEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    public static final String NAME = "drop";

    private static final String ITEM = "item";
    private static final String CLEAR_ITEM = "clear";

    protected final boolean clearItem;
    private final Supplier<Integer> amount;
    private StoryItem item;
    private Material material;

    protected DropItemEvent(StoryItem item, Material material, Supplier<Integer> amount, boolean clearItem) {
        super();
        this.item = item;
        this.material = material;
        this.amount = amount;
        this.clearItem = clearItem;
    }

    public DropItemEvent(Quest quest, Action action, StoryBookBuilder bookBuilder, Toml trigger)
            throws ItemNotFoundException, MissingArgumentException, InvalidArgumentTypeException {
        super(action);

        if (trigger.contains("item")) {
            String itemName = trigger.getString("item");
            if (itemName == null) {
                throw new MissingArgumentException("item");
            }
            this.item = bookBuilder.getItem(itemName);
        } else if (trigger.contains("material")) {
            String materialName = trigger.getString("material");
            this.material = Material.getMaterial(materialName.toUpperCase());
            if (material == null) {
                throw new InvalidArgumentTypeException("invalid material '" + materialName + "'");
            }
        } else {
            throw new MissingArgumentException("item", "material");
        }

        Supplier<Integer> amount;
        try {
            amount = quest.parseAdvancedInt(trigger, "amount");
        } catch (MissingArgumentException e) {
            amount = () -> 1;
        }
        this.amount = amount;

        this.clearItem = trigger.getBoolean(CLEAR_ITEM).equals(Boolean.TRUE);
    }

    @Override
    protected DropItemEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new DropItemEvent<>(this.item != null ? this.item.clone(reader) : null,
                this.material, this.amount, this.clearItem);
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM;
    }

    @StoryEvent
    public void onUserDropItem(UserDropItemEvent e) {

        StoryUser user = (StoryUser) e.getUser();

        if (!this.action.getReader().containsUser(user)) {
            return;
        }

        if (!this.action.isActive()) {
            return;
        }

        if (e.getItemStack().getAmount() < this.amount.get()) {
            return;
        }

        if (this.item != null && !this.item.getItem().equals(ExItemStack.getItem(e.getItemStack(), false))) {
            return;
        }

        if (this.material != null && !this.material.equals(e.getItemStack().getType())) {
            return;
        }

        super.triggerAction(user);

        if (this.clearItem) {
            Server.runTaskLaterSynchrony(() -> e.getItemDrop().remove(), 20, GameStory.getPlugin());
        }
    }
}
