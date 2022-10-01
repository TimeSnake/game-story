package de.timesnake.game.story.elements;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.bukkit.ExVillager;
import de.timesnake.library.entities.pathfinder.ExPathfinderGoal;
import de.timesnake.library.entities.pathfinder.custom.ExCustomPathfinderGoalLocation;
import de.timesnake.library.entities.pathfinder.custom.ExCustomPathfinderGoalLookAtPlayer;
import de.timesnake.library.entities.wrapper.EntityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoryCharacterVillager extends StoryCharacter<ExVillager> {

    public static final String NAME = "villager";

    protected List<ExPathfinderGoal> walkPathfinders = new ArrayList<>();

    public StoryCharacterVillager(String name, String displayName, ExLocation location) {
        super(name, displayName, location);
    }

    public StoryCharacterVillager(String name, Toml character) {
        super(name, character);
    }

    @Override
    public StoryCharacter<ExVillager> clone(StoryReader reader, StoryChapter chapter) {
        StoryCharacterVillager character = new StoryCharacterVillager(this.name, this.displayName,
                this.location.setExWorld(chapter.getWorld()));
        character.reader = reader;
        return character;
    }

    @Override
    protected ExVillager initEntity() {
        ExVillager entity = new ExVillager(this.location.getWorld(), ExVillager.Type.PLAINS, false, false);

        entity.setInvulnerable(true);
        entity.setPersistent(true);

        entity.addPathfinderGoal(1, new ExCustomPathfinderGoalLookAtPlayer(EntityClass.EntityHuman, 1));
        entity.addPathfinderGoal(1, new ExCustomPathfinderGoalLocation(this.location.getX(), this.location.getY(),
                this.location.getZ(), 1, 16, 0.1));

        return entity;
    }

    @Override
    public void spawn() {
        EntityManager.spawnEntity(location.getWorld(), this.entity);
    }

    public void setWalkPathfinders(ExPathfinderGoal... walkPathfinders) {

        for (ExPathfinderGoal oldPathfinderGoal : this.walkPathfinders) {
            this.entity.removePathfinderGoal(oldPathfinderGoal);
        }

        this.walkPathfinders.clear();

        for (ExPathfinderGoal newPathfinderGoal : walkPathfinders) {
            this.entity.addPathfinderGoal(newPathfinderGoal.getPriority(), newPathfinderGoal);
        }

        this.walkPathfinders.addAll(Arrays.asList(walkPathfinders));
    }

    @Override
    public void despawn() {
        this.entity.remove();
    }
}
