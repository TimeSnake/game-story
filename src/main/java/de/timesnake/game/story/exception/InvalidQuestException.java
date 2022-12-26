/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.exception;

public class InvalidQuestException extends RuntimeException {

    public InvalidQuestException() {

    }

    public InvalidQuestException(String message) {
        super(message);
    }
}
