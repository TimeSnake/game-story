package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.entities.entity.extension.EntityExtension;
import de.timesnake.basic.entities.entity.extension.ExEntity;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.entity.LivingEntity;

import java.util.Set;

public abstract class StoryCharacter<Entity extends EntityExtension<? extends ExEntity> & LivingEntity> {

    public static StoryCharacter<?> initCharacter(CharacterFile characterFile, Integer id) {
        String type = characterFile.getCharacterType(id);
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
            case StoryCharacterVillager.NAME:
                return new StoryCharacterVillager(characterFile, id);
            case StoryCharacterPlayer.NAME:
                return new StoryCharacterPlayer(characterFile, id);
        }

        return null;
    }


    protected StoryUser reader;
    protected Set<StoryUser> listeners;

    protected final String name;
    protected final ExLocation location;
    protected final Entity entity;

    public StoryCharacter(String name, ExLocation location) {
        this.name = name;
        this.location = location;
        this.entity = this.initEntity();
    }

    public StoryCharacter(CharacterFile file, int entityId) {
        this.name = file.getCharacterName(entityId);
        this.location = new ExLocation(null, file.getCharacterLocation(entityId));
        this.entity = null;
    }

    public abstract StoryCharacter<Entity> clone(StoryUser reader, Set<StoryUser> listeners);

    public String getName() {
        return name;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ExLocation getLocation() {
        return location;
    }


    protected abstract Entity initEntity();

    public abstract void spawn();

    public abstract void despawn();
}
