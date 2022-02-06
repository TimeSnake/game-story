package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.file.ExFile;

import java.util.Set;

public class ItemFile extends ExFile {

    private static final String ITEMS = "items";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String ENCHANT = "enchant";

    public ItemFile() {
        super("game-story", "items");
    }

    public Set<Integer> getItemIds() {
        return super.getPathIntegerList(ITEMS);
    }

    public String getItemName(int id) {
        return super.getString(getItemPath(id) + "." + NAME);
    }

    public String getItemType(int id) {
        return super.getString(getItemPath(id) + "." + TYPE);
    }

    public boolean isItemEnchanted(int id) {
        return super.getBoolean(getItemPath(id) + "." + ENCHANT);
    }

    public static String getItemPath(int id) {
        return ITEMS + "." + id;
    }
}
