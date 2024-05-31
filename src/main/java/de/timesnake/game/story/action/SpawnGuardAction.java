/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.exception.UnknownGuardTypeException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Difficulty;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.PillagerBuilder;
import de.timesnake.library.entities.entity.RavagerBuilder;
import de.timesnake.library.entities.entity.VindicatorBuilder;
import de.timesnake.library.entities.pathfinder.LocationGoal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.enchantments.Enchantment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SpawnGuardAction extends LocationAction {

  public static final String NAME = "spawn_guard";

  private final Supplier<Integer> amount;
  private final GuardType type;
  private final Set<Mob> guards = new HashSet<>();

  protected SpawnGuardAction(int id, StoryAction next, ExLocation location,
                             StoryCharacter<?> character,
                             Supplier<Integer> amount, GuardType type) {
    super(id, next, location, character);
    this.amount = amount;
    this.type = type;
  }

  public SpawnGuardAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id,
                          List<Integer> diaryPages)
      throws StoryParseException {
    super(bookBuilder, action, id, diaryPages);

    this.amount = quest.parseAdvancedInt(action, "amount");

    String typeName = action.getString("type");
    if (typeName == null) {
      throw new MissingArgumentException("type");
    }
    this.type = GuardType.fromString(typeName);

    if (this.type == null) {
      throw new UnknownGuardTypeException(action.getString("type"));
    }
  }

  @Override
  public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
                           StoryChapter chapter) {
    return new SpawnGuardAction(this.id, clonedNext,
        location.clone().setExWorld(chapter.getWorld()),
        this.character != null ? this.character.clone(reader, chapter) : null, this.amount,
        this.type);
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    this.spawn();
    this.startNext();
  }

  private void spawn() {
    Server.runTaskSynchrony(() -> {
      for (int i = 0; i < this.amount.get(); i++) {
        Mob mob = type.create(this.location, this.reader.getDifficulty());
        mob.persist = true;
        this.guards.add(mob);
        EntityManager.spawnEntity(this.location.getWorld(), mob);
      }
    }, GameStory.getPlugin());
  }

  public enum GuardType {

    PILLAGER() {
      @Override
      public Mob create(Location location, Difficulty difficulty) {
        return new PillagerBuilder()
            .applyOnEntity(e -> {
              e.setPos(location.getX(), location.getY(), location.getZ());
              ExItemStack crossBow = new ExItemStack(Material.CROSSBOW);
              if (difficulty == Difficulty.HARD) {
                crossBow.addEnchantment(Enchantment.QUICK_CHARGE, 3);
              }
              e.setItemSlot(EquipmentSlot.MAINHAND, crossBow.getHandle());
            })
            .addPathfinderGoal(0, e -> new FloatGoal(e))
            .addPathfinderGoal(3, e -> new RangedCrossbowAttackGoal<>(e, 1.0, 15.0F))
            .addPathfinderGoal(7, e -> new LocationGoal(e, location.getX(), location.getY(),
                location.getZ(), 0.9, 32, 5))
            .addPathfinderGoal(8, e -> new RandomStrollGoal(e, 0.6))
            .addPathfinderGoal(9, e -> new LookAtPlayerGoal(e, Mob.class, 15.0F))
            .addTargetGoal(1, e -> new HurtByTargetGoal(e, Monster.class))
            .addTargetGoal(2, e -> new NearestAttackableTargetGoal<>(e, Player.class, true))
            .build(((CraftWorld) location.getWorld()).getHandle());
      }
    },

    VINDICATOR() {
      @Override
      public Mob create(Location location, Difficulty difficulty) {
        return new VindicatorBuilder()
            .applyOnEntity(e -> {
              e.setPos(location.getX(), location.getY(), location.getZ());
              e.setItemSlot(EquipmentSlot.MAINHAND, new ExItemStack(Material.IRON_AXE).getHandle());
            })
            .addPathfinderGoal(0, e -> new FloatGoal(e))
            .addPathfinderGoal(3, e -> switch (difficulty) {
              case EASY -> new MeleeAttackGoal(e, 1.1, false);
              case NORMAL -> new MeleeAttackGoal(e, 1.2, false);
              case HARD -> new MeleeAttackGoal(e, 1.3, false);
            })
            .addPathfinderGoal(7, e -> new LocationGoal(e, location.getX(), location.getY(),
                location.getZ(), 0.9, 32, 5))
            .addPathfinderGoal(8, e -> new RandomStrollGoal(e, 0.6))
            .addPathfinderGoal(9, e -> new LookAtPlayerGoal(e, Player.class, 8.0F))
            .addTargetGoal(1, e -> new HurtByTargetGoal(e, Monster.class))
            .addTargetGoal(2, e -> new NearestAttackableTargetGoal<>(e, Player.class, true))
            .build(((CraftWorld) location.getWorld()).getHandle());
      }
    },

    RAVAGER() {
      @Override
      public Mob create(Location location, Difficulty difficulty) {

        return new RavagerBuilder()
            .applyOnEntity(e -> e.setPos(location.getX(), location.getY(), location.getZ()))
            .addPathfinderGoal(0, e -> new FloatGoal(e))
            .addPathfinderGoal(3, e -> switch (difficulty) {
              case HARD -> new MeleeAttackGoal(e, 1.1, false);
              default -> new MeleeAttackGoal(e, 1.2, false);
            })
            .addPathfinderGoal(7, e -> new LocationGoal(e, location.getX(), location.getY(),
                location.getZ(), 0.9, 32, 5))
            .addPathfinderGoal(8, e -> new RandomStrollGoal(e, 0.4))
            .addPathfinderGoal(9, e -> new LookAtPlayerGoal(e, Player.class, 8.0F))
            .addTargetGoal(1, e -> new HurtByTargetGoal(e, Monster.class))
            .addTargetGoal(2, e -> new NearestAttackableTargetGoal<>(e, Player.class, true))
            .build(((CraftWorld) location.getWorld()).getHandle());
      }
    };

    public static GuardType fromString(String name) {
      return GuardType.valueOf(name.toUpperCase());
    }

    public abstract Mob create(Location location, Difficulty difficulty);
  }
}
