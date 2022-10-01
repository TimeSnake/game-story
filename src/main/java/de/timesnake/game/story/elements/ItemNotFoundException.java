package de.timesnake.game.story.elements;

public class ItemNotFoundException extends Exception {

    private final String name;

    public ItemNotFoundException(String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "Can not find item " + this.name;
    }
}
