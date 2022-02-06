package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;

public class StoryItem {

    private final ExItemStack item;

    public StoryItem(ExItemStack item) {
        this.item = item;
    }

    public StoryItem(ItemFile file, int itemId) {
        Material material = Material.getMaterial(file.getItemType(itemId).toUpperCase());

        if (material == null) {
            throw new IllegalArgumentException("Can not load item type value");
        }

        this.item = new ExItemStack(material, "§6" + file.getItemName(itemId));
        if (file.isItemEnchanted(itemId)) {
            this.item.enchant();
        }
    }

    public StoryItem clone(StoryUser user) {
        return new StoryItem(this.item.cloneWithId());
    }

    public ExItemStack getItem() {
        return item;
    }

}
