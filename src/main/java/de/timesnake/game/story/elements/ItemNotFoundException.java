package de.timesnake.game.story.elements;

public class ItemNotFoundException extends Exception {

    private final int id;

    public ItemNotFoundException(int id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "Can not find item " + this.id;
    }
}
