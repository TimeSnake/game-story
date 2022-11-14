/*
 * workspace.game-story.main
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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.listener.EventManager;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.game.story.user.UserManager;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoryServerManager extends ServerManager implements Listener {

    public static StoryServerManager getInstance() {
        return (StoryServerManager) ServerManager.getInstance();
    }

    private final Map<Integer, StoryBook> bookById = new HashMap<>();

    private EventManager eventManager;
    private UserManager userManager;
    private StoryFile file;

    private ExWorld baseWorld;

    public void onStoryEnable() {
        this.userManager = new UserManager();

        this.file = new StoryFile(new File("plugins/game-story/story.toml"));


        this.baseWorld = Server.getWorld("world");

        this.baseWorld.restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
        this.baseWorld.restrict(ExWorld.Restriction.PLAYER_DAMAGE, false);
        this.baseWorld.restrict(ExWorld.Restriction.FOOD_CHANGE, true);
        this.baseWorld.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
        this.baseWorld.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
        this.baseWorld.restrict(ExWorld.Restriction.DROP_PICK_ITEM, true);
        this.baseWorld.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
        this.baseWorld.setExceptService(true);
        this.baseWorld.setPVP(false);
        this.baseWorld.restrict(ExWorld.Restriction.PLAYER_DAMAGE, true);
        this.baseWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.baseWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        this.eventManager = new EventManager();

        // load chapters from file
        for (Long id : this.file.getBookIds()) {
            StoryBook book = new StoryBookBuilder(id.intValue(), Path.of("plugins", "game-story", id + "")).parseToBook();

            this.bookById.put(id.intValue(), book);

            Server.printText(Plugin.STORY, "Loaded book '" + id + "'");
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

    public Collection<StoryBook> getBooks() {
        return this.bookById.values();
    }

    public ExWorld getBaseWorld() {
        return baseWorld;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
