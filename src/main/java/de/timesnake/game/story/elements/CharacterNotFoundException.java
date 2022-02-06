package de.timesnake.game.story.elements;

public class CharacterNotFoundException extends Exception {

    private final int id;

    public CharacterNotFoundException(int id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "Can not find character " + this.id;
    }
}
