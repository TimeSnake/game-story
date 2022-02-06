package de.timesnake.game.story.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.book.DiaryManager;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.structure.StoryFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.game.story.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoryServerManager extends ServerManager implements Listener {

    public static StoryServerManager getInstance() {
        return (StoryServerManager) ServerManager.getInstance();
    }

    private UserManager userManager;

    private StoryFile file;
    private CharacterFile characterFile;
    private ItemFile itemFile;

    private final Map<Integer, StoryChapter> chaptersById = new HashMap<>();

    private final Map<Integer, StoryCharacter<?>> charactersById = new HashMap<>();
    private final Map<Integer, StoryItem> itemsById = new HashMap<>();

    private ExWorld baseWorld;

    private DiaryManager diaryManager = new DiaryManager();

    public void onStoryEnable() {
        this.userManager = new UserManager();

        this.file = new StoryFile();
        this.characterFile = new CharacterFile();
        this.itemFile = new ItemFile();

        this.baseWorld = Server.getWorld("world");

        // load characters from file
        for (Integer id : this.characterFile.getCharacterIds()) {

            StoryCharacter<?> character = StoryCharacter.initCharacter(this.characterFile, id);

            if (character != null) {
                this.charactersById.put(id, character);
                Server.printText(Plugin.STORY, "Loaded character " + id);
            } else {
                Server.printWarning(Plugin.STORY, "Can not load type of character " + id);
            }

        }

        // load items from file
        for (Integer id : this.itemFile.getItemIds()) {
            this.itemsById.put(id, new StoryItem(this.itemFile, id));

            Server.printText(Plugin.STORY, "Loaded item " + id);
        }

        // load chapters from file
        for (Integer id : this.file.getChapterIds()) {
            StoryChapter chapter = new StoryChapter(id, new ChapterFile(id.toString()));

            this.chaptersById.put(id, chapter);

            Server.printText(Plugin.STORY, "Loaded chapter " + id);
        }

        Server.registerListener(this, GameStory.getPlugin());
    }

    @Override
    public StoryUser loadUser(Player player) {
        return new StoryUser(player);
    }

    public StoryChapter getChapter(Integer id) {
        return this.chaptersById.get(id);
    }


    public StoryCharacter<?> getCharacter(int id) throws CharacterNotFoundException {
        StoryCharacter<?> character = this.charactersById.get(id);
        if (character == null) {
            throw new CharacterNotFoundException(id);
        }

        return character;
    }

    public StoryItem getItem(int id) throws ItemNotFoundException {
        StoryItem item = this.itemsById.get(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }

        return item;
    }

    public Collection<StoryChapter> getChapters() {
        return this.chaptersById.values();
    }

    public ExWorld getBaseWorld() {
        return baseWorld;
    }

    public DiaryManager getDiaryManager() {
        return diaryManager;
    }
}
