/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.PillagerBuilder;
import de.timesnake.library.entities.entity.VillagerBuilder;
import de.timesnake.library.entities.entity.VindicatorBuilder;
import de.timesnake.library.entities.pathfinder.LocationGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

public class StoryCharacterMob extends StoryCharacter<Mob> {

  private final Type type;

  public StoryCharacterMob(String name, String displayName, ExLocation location, Type type) {
    super(name, displayName, location);
    this.type = type;
    this.entity = this.type.initEntity(this.location);
  }

  public StoryCharacterMob(String name, Toml character)
      throws MissingArgumentException, InvalidArgumentTypeException {
    super(name, character);

    String typeString = character.getString("type");
    if (typeString == null) {
      throw new MissingArgumentException("type");
    }

    try {
      this.type = Type.valueOf(typeString.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidArgumentTypeException("Could not load type '" + typeString + "' of character entity");
    }
  }

  @Override
  public StoryCharacter<Mob> clone(StoryReader reader, StoryChapter chapter) {
    StoryCharacterMob character = new StoryCharacterMob(this.name, this.displayName,
        this.location.clone().setExWorld(chapter.getWorld()), this.type);
    character.reader = reader;
    return character;
  }

  @Override
  public void spawn() {
    Server.runTaskSynchrony(() -> {
      this.entity.setCustomNameVisible(false);
      this.entity.persist = true;
      this.entity.removeWhenFarAway(0);
      this.entity.setInvulnerable(true);
      this.entity.setPos(this.location.getX(), this.location.getY(), this.location.getZ());
      this.entity.setRot(this.location.getYaw(), this.location.getPitch());
      EntityManager.spawnEntity(location.getWorld(), this.entity);
    }, GameStory.getPlugin());
  }

  @Override
  public void despawn() {
    this.entity.remove(Entity.RemovalReason.DISCARDED);
  }

  @Override
  public boolean isRotateable() {
    return false;
  }

  public enum Type {

    VILLAGER() {
      @Override
      public Mob initEntity(ExLocation location) {
        return new VillagerBuilder()
            .addPathfinderGoal(1, e -> new LookAtPlayerGoal(e, Player.class, 8.0f))
            .addPathfinderGoal(2, e -> new LocationGoal(e, location.getX(), location.getY(), location.getZ(),
                1, 16, 0.1))
            .build(location.getExWorld().getHandle());
      }
    },
    PILLAGER() {
      @Override
      public Mob initEntity(ExLocation location) {
        return new PillagerBuilder()
            .addPathfinderGoal(1, e -> new LookAtPlayerGoal(e, Player.class, 8.0F))
            .addPathfinderGoal(1, e -> new LocationGoal(e, location.getX(), location.getY(), location.getZ(),
                1, 16, 0.1))
            .build(location.getExWorld().getHandle());
      }
    },
    VINDICATOR() {
      @Override
      public Mob initEntity(ExLocation location) {
        return new VindicatorBuilder()
            .addPathfinderGoal(1, e -> new LookAtPlayerGoal(e, Player.class, 8.0F))
            .addPathfinderGoal(1, e -> new LocationGoal(e, location.getX(), location.getY(), location.getZ(),
                1, 16, 0.1))
            .build(location.getExWorld().getHandle());
      }
    };


    public abstract Mob initEntity(ExLocation location);
  }
}
