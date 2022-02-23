package de.timesnake.game.story.elements;

public class UnknownLocationException extends Exception {

    @Override
    public String getMessage() {
        return "Unknown location";
    }
}
