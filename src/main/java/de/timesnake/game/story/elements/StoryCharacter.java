package de.timesnake.game.story.elements;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.entity.ExtendedCraftEntity;
import de.timesnake.library.entities.entity.extension.ExEntity;
import org.bukkit.entity.LivingEntity;

public abstract class StoryCharacter<Entity extends ExtendedCraftEntity<? extends ExEntity> & LivingEntity> {

    public static StoryCharacter<?> initCharacter(String name, Toml character) {
        String type = character.getString(TYPE);
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
            case StoryCharacterVillager.NAME:
                return new StoryCharacterVillager(name, character);
            case StoryCharacterPlayer.NAME:
                return new StoryCharacterPlayer(name, character);
        }

        return null;
    }

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String LOCATION = "location";
    protected final String name;
    protected final String displayName;
    protected final ExLocation location;
    protected final Entity entity;
    protected StoryReader reader;

    public StoryCharacter(String name, String displayName, ExLocation location) {
        this.name = name;
        this.displayName = displayName;
        this.location = location;
        this.entity = this.initEntity();
    }

    public StoryCharacter(String name, Toml character) {
        this.name = name;
        this.displayName = character.getString(NAME);
        this.location = ExLocation.fromList(character.getList(LOCATION));
        this.entity = null;
    }

    public abstract StoryCharacter<Entity> clone(StoryReader reader, StoryChapter chapter);

    public String getDisplayName() {
        return displayName;
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

    public String getName() {
        return this.name;
    }
}
