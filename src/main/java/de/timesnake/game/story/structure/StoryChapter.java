/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldOption;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StoryChapter implements Iterable<Quest> {

  private final Logger logger = LogManager.getLogger("story.chapter");

  private final String id;
  private final String title;

  private final String endMessage;
  private final Quest firstQuest;
  private final LinkedHashMap<String, StoryCharacter<?>> characterByName = new LinkedHashMap<>();
  private final Diary diary;
  private final List<Integer> playerSizes;
  private final ExWorld world;
  private final Map<Difficulty, Integer> maxDeathsByDifficulty;
  private String previous;
  private String next;
  private StoryBook book;

  public StoryChapter(String id, String title, String endMessage, Diary diary, Quest firstQuest,
                      List<Integer> playerSizes, Map<Difficulty, Integer> maxDeathsByDifficulty, String worldName,
                      Set<StoryCharacter<?>> characters) {
    this.id = id;
    this.title = title;
    this.endMessage = endMessage;
    this.diary = diary;
    this.firstQuest = firstQuest;
    this.firstQuest.setChapter(this);
    this.playerSizes = playerSizes;
    this.maxDeathsByDifficulty = maxDeathsByDifficulty;
    this.world = Server.getWorld(worldName);

    if (this.world == null) {
      this.logger.warn("World '{}' for part '{}' not exists", worldName, this.id);
      return;
    }

    for (StoryCharacter<?> character : characters) {
      this.characterByName.put(character.getName(), character);
    }

    this.world.setPVP(false);
    this.world.setOption(ExWorldOption.ALLOW_BLOCK_PLACE, false);
    this.world.setOption(ExWorldOption.ALLOW_BLOCK_BREAK, false);
    this.world.setOption(ExWorldOption.ALLOW_FLUID_COLLECT, false);
    this.world.setOption(ExWorldOption.ALLOW_FLUID_PLACE, false);
    this.world.setOption(ExWorldOption.BLOCK_BURN_UP, false);
    this.world.setOption(ExWorldOption.ALLOW_BLOCK_IGNITE, false);
    this.world.setOption(ExWorldOption.ALLOW_TNT_PRIME, false);
    this.world.setOption(ExWorldOption.ALLOW_FLINT_AND_STEEL_AND_FIRE_CHARGE, true);
    this.world.setOption(ExWorldOption.ALLOW_LIGHT_UP_INTERACTION, true);
    this.world.setOption(ExWorldOption.FIRE_SPREAD_SPEED, 0f);
    this.world.setOption(ExWorldOption.ENABLE_ENTITY_EXPLOSION, false);
    this.world.setOption(ExWorldOption.ALLOW_ENTITY_BLOCK_BREAK, false);
    this.world.setOption(ExWorldOption.ALLOW_ITEM_FRAME_ROTATE, true);
    this.world.setOption(ExWorldOption.ALLOW_DROP_PICK_ITEM, true);
    this.world.setOption(ExWorldOption.ALLOW_PLACE_IN_BLOCK, true);
    this.world.setOption(ExWorldOption.ALLOW_CAKE_EAT, false);
    this.world.setOption(ExWorldOption.FORBIDDEN_BLOCK_INVENTORIES, List.of(Material.DISPENSER, Material.DROPPER,
        Material.HOPPER));
    this.world.setExceptService(true);
    this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    this.world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);


  }

  private StoryChapter(StoryReader reader, String id, String title, String endMessage,
                       Diary diary, Quest firstQuest, List<Integer> playerSizes,
                       Map<Difficulty, Integer> maxDeathsByDifficulty, ExWorld world,
                       LinkedHashMap<String, StoryCharacter<?>> characterByName) {
    this.id = id;
    this.title = title;
    this.endMessage = endMessage;
    this.world = world;
    this.diary = diary.clone(reader);
    this.playerSizes = playerSizes;
    this.maxDeathsByDifficulty = maxDeathsByDifficulty;

    for (StoryCharacter<?> character : characterByName.values()) {
      this.characterByName.put(character.getName(), character.clone(reader, this));
    }

    this.firstQuest = firstQuest.clone(this, reader, new HashMap<>());
  }

  public StoryChapter clone(StoryReader reader) {
    return new StoryChapter(reader, this.id, this.title, this.endMessage, this.diary, this.firstQuest,
        this.playerSizes, this.maxDeathsByDifficulty,
        Server.getWorldManager().cloneWorld(this.world.getName() + "_" + reader.getId(), this.world),
        this.characterByName);
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getEndMessage() {
    return endMessage;
  }

  public String getPrevious() {
    return previous;
  }

  protected void setPrevious(String previous) {
    this.previous = previous;
  }

  public String getNext() {
    return next;
  }

  protected void setNext(String next) {
    this.next = next;
  }

  public StoryBook getBook() {
    return book;
  }

  protected void setBook(StoryBook book) {
    this.book = book;
  }

  public Quest getFirstQuest() {
    return this.firstQuest;
  }

  public Quest getLastQuest() {
    return this.firstQuest.lastQuest();
  }

  public Quest getQuest(String name) {
    for (Quest quest : this) {
      if (quest.getName().equals(name)) {
        return quest;
      }
    }
    return null;
  }

  public Diary getDiary() {
    return diary;
  }

  public void spawnCharacters() {
    Server.runTaskLaterAsynchrony(() -> {
      for (StoryCharacter<?> character : this.characterByName.values()) {
        character.spawn();
      }
    }, 10, GameStory.getPlugin());
  }

  public void despawnCharacters() {
    this.characterByName.values().forEach(StoryCharacter::despawn);
  }

  public StoryCharacter<?> getCharacter(String name) {
    return this.characterByName.get(name);
  }

  public List<Integer> getPlayerSizes() {
    return playerSizes;
  }

  public ExWorld getWorld() {
    return world;
  }

  public Integer getMaxDeaths(Difficulty difficulty) {
    return maxDeathsByDifficulty.get(difficulty);
  }

  @NotNull
  @Override
  public Iterator<Quest> iterator() {
    return new QuestIterator(this.firstQuest);
  }

  public static class QuestIterator implements Iterator<Quest> {

    private final Stack<Quest> traversal;

    public QuestIterator(Quest first) {
      traversal = new Stack<>();
      traversal.push(first);
    }

    public boolean hasNext() {
      return !traversal.isEmpty();
    }

    public Quest next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      Quest current = traversal.pop();

      List<? extends Quest> nextQuests = current.getNextQuests();
      Collections.reverse(nextQuests);
      nextQuests.forEach(traversal::push);

      return current;
    }
  }
}
