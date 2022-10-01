package de.timesnake.game.story.elements;

public class CharacterNotFoundException extends Exception {

    private final String name;

    public CharacterNotFoundException(String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "Can not find character " + this.name;
    }
}
