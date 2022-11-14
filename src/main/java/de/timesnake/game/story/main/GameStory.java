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

package de.timesnake.game.story.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.story.book.StoryCmd;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.server.StoryServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameStory extends JavaPlugin {

    public static GameStory getPlugin() {
        return plugin;
    }

    private static GameStory plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new StoryServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "story", new StoryCmd(), Plugin.STORY);
        StoryServerManager.getInstance().onStoryEnable();
    }
}
