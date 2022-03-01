package de.timesnake.game.story.server;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.structure.StoryChapter;

import java.util.Collection;

public class StoryServer {

    public static final int PART_PRICE = 200;

    private static final StoryServerManager server = StoryServerManager.getInstance();

    public static StoryChapter getChapter(Integer id) {
        return server.getChapter(id);
    }

    public static StoryCharacter<?> getCharater(int id) throws CharacterNotFoundException {
        return server.getCharacter(id);
    }

    public static StoryItem getItem(int id) throws ItemNotFoundException {
        return server.getItem(id);
    }

    public static Collection<StoryChapter> getChapters() {
        return server.getChapters();
    }

    public static ExWorld getBaseWorld() {
        return server.getBaseWorld();
    }

    public static ExWorld getStoryWorldTemplate() {
        return server.getStoryWorldTemplate();
    }
}
