/*
 * Copyright (C) 2023 timesnake
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
