package de.timesnake.game.story.elements;

public class InvalidQuestException extends RuntimeException {

    public InvalidQuestException() {

    }

    public InvalidQuestException(String message) {
        super(message);
    }
}
