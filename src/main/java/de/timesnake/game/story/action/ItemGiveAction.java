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
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemGiveAction extends LocationAction {

    public static final String NAME = "item_give";
    private final int amount;
    private StoryItem item;
    private Material material;

    public ItemGiveAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, StoryItem item,
                          Material material, int amount) {
        super(id, next, location, character);
        this.item = item;
        this.material = material;
        this.amount = amount;
    }

    public ItemGiveAction(Toml action, int id, List<Integer> diaryPages) throws CharacterNotFoundException,
            ItemNotFoundException, UnknownLocationException, MissingArgumentException, InvalidArgumentTypeException {
        super(action, id, diaryPages);
        if (action.contains("item")) {
            String itemName = action.getString("item");
            if (itemName == null) {
                throw new MissingArgumentException("item");
            }
            this.item = StoryServer.getItem(itemName);
        } else if (action.contains("material")) {
            String materialName = action.getString("material");
            this.material = Material.getMaterial(materialName.toUpperCase());
            if (material == null) {
                throw new InvalidArgumentTypeException("invalid material '" + materialName + "'");
            }
        } else {
            throw new MissingArgumentException("item", "material");
        }

        if (action.contains("amount")) {
            Long amount = action.getLong("amount");
            if (amount == null) {
                throw new InvalidArgumentTypeException("invalid item amount");
            }

            this.amount = amount.intValue();
        } else {
            this.amount = 1;
        }

    }


    @Override
    public ItemGiveAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemGiveAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                quest.getChapter().getCharacter(character.getName()),
                this.item != null ? this.item.clone(reader) : null,
                this.material, this.amount);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (this.item != null) {
            this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0),
                    this.item.getItem().asQuantity(this.amount));
        } else {
            this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0),
                    new ItemStack(this.material, this.amount));
        }
        this.startNext();
    }
}
