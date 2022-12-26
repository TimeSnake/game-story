/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.exception;

public class MissingArgumentException extends StoryParseException {

    public MissingArgumentException(String... type) {
        super("Missing '" + String.join("' or '", type) + "' argument");
    }
}
