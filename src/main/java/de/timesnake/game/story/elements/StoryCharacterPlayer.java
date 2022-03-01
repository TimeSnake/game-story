package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.entities.entity.bukkit.ExPlayer;
import de.timesnake.game.story.user.StoryUser;

import java.util.Set;

public class StoryCharacterPlayer extends StoryCharacter<ExPlayer> {

    public static final String NAME = "player";

    private static final String SKIN = "skin";
    private static final String SKIN_VALUE = "value";
    private static final String SKIN_SIGNATURE = "signature";


    private final String skinValue;
    private final String skinSignature;

    private boolean spawned = false;

    public StoryCharacterPlayer(Integer id, String name, ExLocation location, String skinValue, String skinSignature) {
        super(id, name, location);
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;

        this.entity.setTextures(this.skinValue, this.skinSignature);
    }

    public StoryCharacterPlayer(CharacterFile file, int entityId) {
        super(file, entityId);

        this.skinValue = file.getCharacterValue(entityId, SKIN + "." + SKIN_VALUE);
        this.skinSignature = file.getCharacterValue(entityId, SKIN + "." + SKIN_SIGNATURE);
    }

    @Override
    public StoryCharacter<ExPlayer> clone(StoryUser reader, Set<StoryUser> listeners) {
        StoryCharacterPlayer character = new StoryCharacterPlayer(this.id, this.name, this.location.clone().setExWorld(reader.getStoryWorld()), this.skinValue, this.skinSignature);
        character.reader = reader;
        character.listeners = listeners;
        return character;
    }

    @Override
    protected ExPlayer initEntity() {
        ExPlayer player = new ExPlayer(this.location.getWorld(), this.name);

        player.setPosition(this.location.getX(), this.location.getY(), this.location.getZ());

        return player;
    }

    @Override
    public void spawn() {
        if (this.spawned) {
            return;
        }

        this.spawned = true;

        Server.getEntityManager().spawnPlayer(this.reader, this.entity);
        Server.getEntityManager().spawnPlayer(this.listeners, this.entity);
    }

    @Override
    public void despawn() {
        Server.getEntityManager().removePlayer(this.reader, this.entity);
        Server.getEntityManager().removePlayer(this.listeners, this.entity);
    }
}
