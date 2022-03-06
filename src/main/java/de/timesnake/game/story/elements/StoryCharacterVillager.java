package de.timesnake.game.story.elements;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.entities.EntityManager;
import de.timesnake.basic.entities.entity.bukkit.ExVillager;
import de.timesnake.basic.entities.pathfinder.ExPathfinderGoal;
import de.timesnake.basic.entities.pathfinder.ExPathfinderGoalLocation;
import de.timesnake.basic.entities.pathfinder.ExPathfinderGoalLookAtPlayer;
import de.timesnake.basic.entities.wrapper.EntityClass;
import de.timesnake.game.story.user.StoryUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class StoryCharacterVillager extends StoryCharacter<ExVillager> {

    public static final String NAME = "villager";

    protected List<ExPathfinderGoal> walkPathfinders = new ArrayList<>();

    public StoryCharacterVillager(Integer id, String name, ExLocation location) {
        super(id, name, location);
    }

    public StoryCharacterVillager(CharacterFile file, int entityId) {
        super(file, entityId);
    }

    @Override
    public StoryCharacter<ExVillager> clone(StoryUser reader, Set<StoryUser> listeners) {
        StoryCharacterVillager character = new StoryCharacterVillager(this.id, this.name, this.location.setExWorld(reader.getStoryWorld()));
        character.reader = reader;
        character.listeners = listeners;
        return character;
    }

    @Override
    protected ExVillager initEntity() {
        ExVillager entity = new ExVillager(this.location.getWorld(), ExVillager.Type.PLAINS, false, false);

        entity.setInvulnerable(true);
        entity.setPersistent(true);

        entity.addPathfinderGoal(1, new ExPathfinderGoalLookAtPlayer(EntityClass.EntityHuman, 1));
        entity.addPathfinderGoal(1, new ExPathfinderGoalLocation(this.location.getX(), this.location.getY(), this.location.getZ(), 1, 16, 0.1));

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
