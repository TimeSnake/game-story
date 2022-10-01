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
