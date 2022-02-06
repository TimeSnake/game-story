package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.library.basic.util.Triple;

import java.util.Set;

public class CharacterFile extends ExFile {

    private static final String CHARACTERS = "characters";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String LOCATION = "location";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";


    public CharacterFile() {
        super("game-story", "characters");
    }

    public Set<Integer> getCharacterIds() {
        return super.getPathIntegerList(CHARACTERS);
    }

    public String getCharacterName(int id) {
        return super.getString(getCharacterPath(id) + "." + NAME);
    }

    public String getCharacterType(int id) {
        return super.getString(getCharacterPath(id) + "." + TYPE);
    }

    public Triple<Double, Double, Double> getCharacterLocation(int id) {
        return super.getDoubleTriple(getCharacterPath(id) + "." + LOCATION, X, Y, Z);
    }

    public String getCharacterValue(int id, String value) {
        return super.getString(getCharacterPath(id) + "." + value);
    }

    public static String getCharacterPath(int id) {
        return CHARACTERS + "." + id;
    }
}
