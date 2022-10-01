package de.timesnake.game.story.server;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.structure.StoryBook;

import java.util.Collection;

public class StoryServer {

    public static final int PART_PRICE = 200;

    public static StoryBook getBook(Integer id) {
        return server.getBook(id);
    }

    public static StoryCharacter<?> getCharater(String name) throws CharacterNotFoundException {
        return server.getCharacter(name);
    }

    public static StoryItem getItem(String name) throws ItemNotFoundException {
        return server.getItem(name);
    }

    public static Collection<StoryBook> getBooks() {
        return server.getBooks();
    }

    public static ExWorld getBaseWorld() {
        return server.getBaseWorld();
    }

    public static ExWorld getStoryWorldTemplate() {
        return server.getStoryWorldTemplate();
    }

    private static final StoryServerManager server = StoryServerManager.getInstance();
}
