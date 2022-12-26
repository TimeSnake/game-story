/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.exception;

public class UnknownGuardTypeException extends StoryParseException {

    public UnknownGuardTypeException(String name) {
        super("Unknown guard type '" + name + "'");
    }
}
