/*
 * timesnake.game-story.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.ItemNotFoundException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DropItemEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements Listener {

    public static final String NAME = "drop";

    private static final String ITEM = "item";
    private static final String CLEAR_ITEM = "clear";

    protected final boolean clearItem;
    private final int amount;
    private StoryItem item;
    private Material material;

    protected DropItemEvent(StoryItem item, Material material, int amount, boolean clearItem) {
        super();
        this.item = item;
        this.material = material;
        this.amount = amount;
        this.clearItem = clearItem;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public DropItemEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger)
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

        if (trigger.contains("amount")) {
            Long amount = trigger.getLong("amount");
            if (amount == null) {
                throw new InvalidArgumentTypeException("invalid item amount");
            }

            this.amount = amount.intValue();
        } else {
            this.amount = 1;
        }

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

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {

        StoryUser user = (StoryUser) e.getUser();

        if (!this.action.getReader().containsUser(user)) {
            return;
        }

        if (!this.action.isActive()) {
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
