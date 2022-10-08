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

package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.file.ExToml;

import java.io.File;
import java.util.List;

public class StoryFile {

    private static final String BOOKS = "books";

    private final ExToml file;

    public StoryFile(File file) {
        this.file = new ExToml(file);
    }

    public List<Long> getBookIds() {
        return this.file.getList(BOOKS);
    }
}
