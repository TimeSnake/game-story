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

package de.timesnake.game.story.elements;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.user.StoryReader;
import org.bukkit.Material;

public class StoryItem {

    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String ENCHANT = "enchant";

    private final ExItemStack item;

    public StoryItem(ExItemStack item) {
        this.item = item;
    }

    public StoryItem(Toml item, String name) {
        Material material = Material.getMaterial(item.getString(TYPE).toUpperCase());

        if (material == null) {
            throw new IllegalArgumentException("Can not load item type value");
        }

        this.item = new ExItemStack(material, "§6" + item.getString(NAME));
        if (item.getBoolean(ENCHANT, false)) {
            this.item.enchant();
        }
    }

    public StoryItem clone(StoryReader reader) {
        return new StoryItem(this.item.cloneWithId());
    }

    public ExItemStack getItem() {
        return item;
    }

}
