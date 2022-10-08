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

package de.timesnake.game.story.server;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.BookFile;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.game.story.user.UserManager;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoryServerManager extends ServerManager implements Listener {

    public static StoryServerManager getInstance() {
        return (StoryServerManager) ServerManager.getInstance();
    }

    private final Map<Integer, StoryBook> bookById = new HashMap<>();
    private final Map<String, StoryCharacter<?>> characterByName = new HashMap<>();
    private final Map<String, StoryItem> itemByName = new HashMap<>();
    private UserManager userManager;
    private StoryFile file;
    private CharacterFile characterFile;
    private ItemFile itemFile;
    private ExWorld baseWorld;
    private ExWorld storyWorldTemplate;

    public void onStoryEnable() {
        this.userManager = new UserManager();

        this.file = new StoryFile(new File("plugins/game-story/story.toml"));
        this.characterFile = new CharacterFile(new File("plugins/game-story/characters.toml"));
        this.itemFile = new ItemFile(new File("plugins/game-story/items.toml"));

        this.baseWorld = Server.getWorld("world");
        this.storyWorldTemplate = Server.getWorld("story");

        this.baseWorld.allowEntityExplode(false);
        this.baseWorld.allowPlayerDamage(true);
        this.baseWorld.allowFoodChange(false);
        this.baseWorld.allowBlockBurnUp(false);
        this.baseWorld.allowEntityBlockBreak(false);
        this.baseWorld.allowDropPickItem(false);
        this.baseWorld.allowBlockBreak(false);
        this.baseWorld.setExceptService(true);
        this.baseWorld.setPVP(false);
        this.baseWorld.allowPlayerDamage(false);
        this.baseWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.baseWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        // load characters from file
        for (Map.Entry<String, Toml> entry : this.characterFile.getCharacterTables().entrySet()) {
            String characterName = entry.getKey();
            StoryCharacter<?> character = StoryCharacter.initCharacter(characterName, entry.getValue());

            if (character != null) {
                if (this.characterByName.containsKey(characterName)) {
                    Server.printWarning(Plugin.STORY, "Duplicate character name " + characterName);
                    continue;
                }
                this.characterByName.put(characterName, character);
                Server.printText(Plugin.STORY, "Loaded character " + characterName);
            } else {
                Server.printWarning(Plugin.STORY, "Can not load type of character " + characterName);
            }

        }

        // load items from file
        for (Map.Entry<String, Toml> entry : this.itemFile.getItemTables().entrySet()) {
            String itemName = entry.getKey();
            this.itemByName.put(itemName, new StoryItem(entry.getValue(), itemName));

            Server.printText(Plugin.STORY, "Loaded item " + itemName);
        }

        // load chapters from file
        for (Long id : this.file.getBookIds()) {
            StoryBook book = new BookFile(id.intValue(), new File("plugins/game-story/book_" + id + ".toml")).parseToBook();

            this.bookById.put(id.intValue(), book);

            Server.printText(Plugin.STORY, "Loaded book " + id);
        }

        Server.registerListener(this, GameStory.getPlugin());
    }

    @Override
    public StoryUser loadUser(Player player) {
        return new StoryUser(player);
    }

    public StoryBook getBook(Integer id) {
        return this.bookById.get(id);
    }


    public StoryCharacter<?> getCharacter(String name) throws CharacterNotFoundException {
        StoryCharacter<?> character = this.characterByName.get(name);
        if (character == null) {
            throw new CharacterNotFoundException(name);
        }

        return character;
    }

    public StoryItem getItem(String name) throws ItemNotFoundException {
        StoryItem item = this.itemByName.get(name);
        if (item == null) {
            throw new ItemNotFoundException(name);
        }

        return item;
    }

    public Collection<StoryBook> getBooks() {
        return this.bookById.values();
    }

    public ExWorld getBaseWorld() {
        return baseWorld;
    }

    public ExWorld getStoryWorldTemplate() {
        return this.storyWorldTemplate;
    }
}
