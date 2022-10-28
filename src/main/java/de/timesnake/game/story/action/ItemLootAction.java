/*
 * game-story.main
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
        } else {
            throw new StoryGamePlayException("Could not set loot item, block has no inventory");
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
