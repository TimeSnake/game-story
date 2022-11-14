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

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.listener.EventManager;
import de.timesnake.game.story.structure.StoryBook;

import java.util.Collection;

public class StoryServer {

    public static final int PART_PRICE = 200;

    public static StoryBook getBook(Integer id) {
        return server.getBook(id);
    }

    public static Collection<StoryBook> getBooks() {
        return server.getBooks();
    }

    public static ExWorld getBaseWorld() {
        return server.getBaseWorld();
    }

    public static EventManager getEventManager() {
        return server.getEventManager();
    }

    private static final StoryServerManager server = StoryServerManager.getInstance();
}
