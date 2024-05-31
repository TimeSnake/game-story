/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract sealed class Quest implements Iterable<StoryAction> permits MainQuest,
    OptionalQuest {

  protected static final String START_LOCATION = "location";

  protected final Logger logger = LogManager.getLogger("story.quest");

  protected final String name;
  protected final ExLocation startLocation;
  protected StoryAction firstAction;
  protected StoryChapter chapter;

  protected StoryReader reader;
  protected String selectedQuest;

  protected boolean skip;
  protected Collection<Quest> questsToSkipAtStart = new LinkedList<>();
  protected Collection<Quest> questsToSkipAtEnd = new LinkedList<>();
  protected int lastActionId;

  protected Map<String, Supplier<?>> varSupplier;

  public Quest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
               Map<String, Supplier<?>> varSupplier, StoryAction firstAction, int lastActionId) {
    this.chapter = chapter;
    this.name = name;
    this.reader = reader;
    this.startLocation = startLocation.clone().setExWorld(chapter.getWorld());
    this.varSupplier = new HashMap<>();
    for (Map.Entry<String, Supplier<?>> entry : varSupplier.entrySet()) {
      Object value = entry.getValue().get();
      this.varSupplier.put(entry.getKey(), () -> value);
    }

    this.firstAction = firstAction.clone(this, reader, chapter);
    this.lastActionId = lastActionId;
  }

  public Quest(StoryBookBuilder bookBuilder, Toml quest, String name) throws InvalidArgumentTypeException {
    this.name = name;
    this.startLocation = ExLocation.fromList(quest.getList(START_LOCATION));
    this.varSupplier = new HashMap<>();

    if (quest.containsTable("var")) {
      for (Map.Entry<String, Object> entry : quest.getTable("var").entrySet()) {
        Supplier<?> supplier = this.parseVar(entry.getValue());
        if (supplier == null) {
          throw new InvalidArgumentTypeException("Could not parse value of variable '" + entry.getKey() + "'");
        }
        this.varSupplier.put(entry.getKey(), supplier);
      }
    }
  }

  protected void setFirstAction(StoryAction firstAction) {
    this.firstAction = firstAction;
    this.firstAction.setQuest(this);
  }

  public abstract Quest clone(StoryChapter chapter, StoryReader reader, Map<String, Quest> visited);

  protected void cloneSkipQuests(StoryChapter chapter, StoryReader reader, Quest cloned, Map<String, Quest> visited) {
    for (Quest quest : this.questsToSkipAtStart) {
      cloned.questsToSkipAtStart.add(quest.clone(chapter, reader, visited));
    }

    for (Quest quest : this.questsToSkipAtEnd) {
      cloned.questsToSkipAtEnd.add(quest.clone(chapter, reader, visited));
    }
  }

  public abstract void forEachNext(Consumer<Quest> consumer, Set<Quest> visited);

  public String getName() {
    return name;
  }

  public StoryChapter getChapter() {
    return chapter;
  }

  public void setChapter(StoryChapter chapter) {
    this.forEachNext(q -> q.chapter = chapter, new HashSet<>());
  }

  public boolean start(boolean teleport, boolean spawnEntities) {
    if (this.skip) {
      return false;
    }

    this.logger.info("{} enabled quest '{}'",
        this.reader.getUsers().stream().map(User::getName).collect(Collectors.joining(", ")), this.name);

    for (Quest quest : this.questsToSkipAtStart) {
      quest.skip();
    }

    if (teleport) {
      this.reader.forEach(u -> u.teleport(this.startLocation));

      this.reader.forEach(u -> {
        u.addPotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1);
        u.lockLocation();
      });

      Server.runTaskTimerSynchrony((t) -> {
        for (int angle = 0; angle < 360; angle += 10) {
          double x = (Math.sin(angle)) * 0.7;
          double z = (Math.cos(angle)) * 0.7;

          Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(102, 0, 102), 1.2f);
          this.startLocation.getWorld().spawnParticle(Particle.PORTAL,
              this.startLocation.getX() + x, this.startLocation.getY(), this.startLocation.getZ() + z,
              8, 0, 1.5, 0, 5, dust);
        }
      }, 5, true, 0, 10, GameStory.getPlugin());

      Server.runTaskLaterSynchrony(() -> this.reader.forEach(User::unlockLocation), 20 * 2, GameStory.getPlugin());
    }

    if (spawnEntities) {
      Server.runTaskLaterSynchrony(() -> {
        int delay = 0;
        for (StoryAction action : this) {
          Server.runTaskLaterSynchrony(action::spawnEntities, delay, GameStory.getPlugin());
          delay += 10;
        }

        this.firstAction.start();
      }, 20, GameStory.getPlugin());
    }

    return true;
  }

  public void finish() {
    this.questsToSkipAtEnd.forEach(Quest::skip);

    Server.runTaskLaterSynchrony(this::clearEntities, 10 * 20, GameStory.getPlugin());

    this.logger.info("{} completed quest '{}'",
        this.reader.getUsers().stream().map(User::getName).collect(Collectors.joining(", ")), this.name);

    this.reader.onCompletedQuest(this);
  }

  public void skip() {
    this.skip = true;
    this.forEach(StoryAction::stop);

    this.logger.info("{} skipped quest '{}'",
        this.reader.getUsers().stream().map(User::getName).collect(Collectors.joining(", ")), this.name);
  }

  public abstract Collection<OptionalQuest> startNextOptionals();

  public abstract Quest lastQuest();

  public void setSelectedQuest(String name) {
    this.selectedQuest = name;
  }

  @Override
  public @NotNull Iterator<StoryAction> iterator() {
    return new StoryAction.ActionIterator(this.firstAction);
  }

  public void clearEntities() {
    this.forEach(StoryAction::despawnEntities);
  }

  public ExLocation getStartLocation() {
    return this.startLocation;
  }

  public Map<String, Supplier<?>> getVars() {
    return this.varSupplier;
  }

  public Supplier<Integer> parseAdvancedInt(Toml toml, String key)
      throws InvalidArgumentTypeException, MissingArgumentException {
    Object value = toml.toMap().get(key);
    if (value == null) {
      throw new MissingArgumentException(key);
    }
    return this.parseAdvancedInt(value);
  }

  public Supplier<Integer> parseAdvancedInt(Object value) throws InvalidArgumentTypeException {
    if (value instanceof Long) {
      return () -> ((Long) value).intValue();
    } else {
      String s = ((String) value).replace(" ", "");
      if (s.contains("..")) {
        String[] bounds = s.split("\\.\\.");
        int lower;
        int upper;
        try {
          lower = Integer.parseInt(bounds[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          return null;
        }

        try {
          upper = Integer.parseInt(bounds[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          return null;
        }

        if (lower > upper) {
          return null;
        }

        return () -> new Random().nextInt(lower, upper);
      } else if (s.contains(",")) {
        String[] values = s.split(",");
        int[] numbers = new int[values.length];
        for (int i = 0; i < values.length; i++) {
          try {
            numbers[i] = Integer.parseInt(values[i]);
          } catch (NumberFormatException e) {
            return null;
          }
        }
        return () -> numbers[new Random().nextInt(numbers.length)];
      } else if (s.startsWith("$")) {
        String varName = s.replaceFirst("\\$", "");
        if (!(this.varSupplier.get(varName).get() instanceof Integer)) {
          throw new InvalidArgumentTypeException("Invalid var type");
        }
        return () -> ((Integer) this.varSupplier.get(varName).get());
      }
    }
    throw new InvalidArgumentTypeException("Invalid var type");
  }

  public Supplier<String> parseString(String value) {
    if (value == null) {
      return null;
    }
    if (value.isBlank()) {
      return () -> value;
    }

    String[] splitByVars = value.split("\\$\\{");
    List<Object> result = new LinkedList<>();
    result.add(splitByVars[0]);
    for (int i = 1; i < splitByVars.length; i++) {
      String[] splitByBlank = splitByVars[i].split("}", 2);
      result.add(this.getVars().get(splitByBlank[0]));
      if (splitByBlank.length >= 2) {
        result.add(splitByBlank[1]);
      }
    }
    return () -> {
      StringBuilder sb = new StringBuilder();
      for (Object obj : result) {
        if (obj instanceof String) {
          sb.append(obj);
        } else if (obj instanceof Supplier<?>) {
          sb.append(((Supplier<?>) obj).get());
        }
      }
      return sb.toString();
    };
  }

  public Supplier<?> parseVar(Object value) {
    if (value instanceof String) {
      Supplier<?> supplier;
      try {
        supplier = this.parseAdvancedInt(value);
        if (supplier != null) {
          return supplier;
        }
      } catch (InvalidArgumentTypeException ignored) {
      }
    } else if (value instanceof Long) {
      return () -> ((Long) value).intValue();
    }
    return () -> value;
  }

  public abstract void addNextQuest(Quest quest);

  public abstract List<? extends Quest> getNextQuests();

  public void addQuestsToSkipAtStart(Quest quest) {
    this.questsToSkipAtStart.add(quest);
  }

  public void addQuestsToSkipAtEnd(Quest quest) {
    this.questsToSkipAtEnd.add(quest);
  }

  public int getLastActionId() {
    return lastActionId;
  }

  public void setLastActionId(int actionId) {
    this.lastActionId = actionId;
  }

  public enum Type {
    MAIN,
    OPTIONAL
  }
}
