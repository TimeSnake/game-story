/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.file.ExToml;
import de.timesnake.game.story.action.*;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.element.*;
import de.timesnake.game.story.event.*;
import de.timesnake.game.story.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class StoryBookBuilder {

  private static final String NAME = "name";
  private static final String END_MESSAGE = "end_message";

  private final Logger logger = LogManager.getLogger("story.book.builder");

  private final String id;
  private final Path folder;
  private final ExToml bookToml;
  private final Map<String, StoryCharacter<?>> characterByName = new HashMap<>();
  private final Map<String, StoryItem> itemByName = new HashMap<>();
  private final Set<AmbientSound> ambientSounds = new HashSet<>();

  public StoryBookBuilder(String id, Path folder) {
    this.id = id;
    this.folder = folder;
    this.bookToml = new ExToml(folder.resolve("book.toml").toFile());
  }

  public StoryBook parseToBook() {
    this.logger.info("Loading book {}", this.id);

    String bookName = this.bookToml.getString(NAME);

    // load characters
    CharacterFile characterFile = new CharacterFile(folder.resolve("characters.toml").toFile());

    for (Map.Entry<String, Toml> entry : characterFile.getCharacterTables().entrySet()) {
      String characterName = entry.getKey();
      StoryCharacter<?> character = null;
      try {
        character = StoryCharacter.initCharacter(characterName, entry.getValue());
      } catch (MissingArgumentException | InvalidArgumentTypeException e) {
        this.logger.info("Character: {}", e.getMessage());
      }

      if (character != null) {
        if (characterByName.containsKey(characterName)) {
          this.logger.warn("Duplicate character name '{}'", characterName);
          continue;
        }
        characterByName.put(characterName, character);
        this.logger.info("Loaded character {}", characterName);
      } else {
        this.logger.warn("Could not load type of character '{}'", characterName);
      }

    }

    // load items
    ItemFile itemFile = new ItemFile(folder.resolve("items.toml").toFile());

    for (Map.Entry<String, Toml> entry : itemFile.getItemTables().entrySet()) {
      String itemName = entry.getKey();
      itemByName.put(itemName, new StoryItem(entry.getValue(), itemName));

      this.logger.info("Loaded item '{}'", itemName);
    }

    // load chapters
    List<String> chapterNames = this.bookToml.getList("chapters");

    LinkedHashMap<String, StoryChapter> chapterByName = new LinkedHashMap<>();

    StoryChapter currentChapter = null;

    for (String chapterName : chapterNames) {
      ExToml chapter = new ExToml(this.folder.resolve("chapter_" + chapterName + ".toml").toFile());

      String chapterDisplayName = chapter.getString(NAME);
      String chapterEndMessage = chapter.getString(END_MESSAGE);
      List<Long> playerSizes = chapter.getList("players");
      if (playerSizes == null) {
        this.logger.warn("Book {} Chapter {}: Missing argument 'players'", this.id, chapterName);
        playerSizes = List.of(1L);
      }

      // max deaths
      Map<Difficulty, Integer> maxDeathsByDifficulty = new HashMap<>();

      for (Difficulty difficulty : Difficulty.values()) {
        Long maxDeaths = chapter.getLong(difficulty.name().toLowerCase() + ".max_deaths");
        maxDeathsByDifficulty.put(difficulty, maxDeaths != null ? maxDeaths.intValue() : null);
      }

      // ambient sounds
      Toml ambientSoundEntries = chapter.getTable("ambient_sounds");
      if (ambientSoundEntries != null) {
        for (String name : ambientSoundEntries.toMap().keySet()) {
          try {
            this.ambientSounds.add(new AmbientSound(ambientSoundEntries.getTable(name), name));
          } catch (MissingArgumentException | InvalidArgumentTypeException e) {
            this.logger.warn("Ambient Sound {}", e.getMessage());
          }
        }
      }

      String worldName = chapter.getString("world");

      Toml diaryTable = chapter.getTable("diary");
      Diary diary = new Diary(diaryTable);

      Set<String> characterNames = new HashSet<>();

      String startQuest = chapter.getString("start_quest");

      Set<String> questsToLoad = new HashSet<>();
      Map<String, Quest> loadedQuestByName = new HashMap<>();
      Map<String, List<String>> nextQuestsByQuestName = new HashMap<>();
      Map<String, List<String>> questsToSkipAtStartByQuestName = new HashMap<>();
      Map<String, List<String>> questsToSkipAtEndByQuestName = new HashMap<>();

      questsToLoad.add(startQuest);

      while (!questsToLoad.isEmpty()) {
        String questName = questsToLoad.iterator().next();
        questsToLoad.remove(questName);

        if (loadedQuestByName.containsKey(questName)) {
          continue;
        }

        Toml quest = chapter.getTable("quest." + questName);

        if (quest == null) {
          this.logger.warn("Referenced unknown quest '{}'", questName);
          continue;
        }

        String typeName = quest.getString("type");
        if (typeName == null) {
          this.logger.warn("Book {} Chapter {} Quest {}: Missing type argument", this.id, chapterName, questName);
        }
        Quest.Type type = Quest.Type.valueOf(typeName.toUpperCase());

        Quest currentQuest;

        try {
          if (type.equals(Quest.Type.MAIN)) {
            currentQuest = new MainQuest(this, quest, questName);
          } else {
            currentQuest = new OptionalQuest(this, quest, questName);
          }
        } catch (StoryParseException e) {
          this.logger.warn("Book {} Chapter {} Quest {}: {}", e.getMessage(), this.id, chapterName, questName);
          continue;
        }

        loadedQuestByName.put(currentQuest.getName(), currentQuest);

        StoryAction first = null;
        StoryAction previous = null;
        StoryAction current;

        int actionId = 1;
        for (; quest.contains(String.valueOf(actionId)); actionId++) {
          Toml action = quest.getTable(String.valueOf(actionId));
          try {
            current = this.getActionFromFile(currentQuest, action, actionId);
          } catch (StoryParseException e) {
            this.logger.warn("Book {} Chapter {} Quest {} Action {}: {}", this.id, chapterName, questName,
                actionId, e.getMessage());
            continue;
          }

          characterNames.addAll(current.getCharacterNames());

          if (actionId == 1) {
            first = current;
          }

          if (previous != null) {
            previous.setNext(current);
          }

          previous = current;
        }

        actionId--;

        List<String> questsToSkipAtStart = quest.getList("skip_at_start");
        List<String> questsToSkipAtEnd = quest.getList("skip_at_end");

        if (questsToSkipAtStart != null) {
          questsToSkipAtStartByQuestName.put(questName, questsToSkipAtStart);
        }

        if (questsToSkipAtEnd != null) {
          questsToSkipAtEndByQuestName.put(questName, questsToSkipAtEnd);
        }

        List<String> next = quest.getList("next");
        nextQuestsByQuestName.put(questName, next);

        if (next != null) {
          questsToLoad.addAll(next);
        }

        if (first != null) {
          currentQuest.setFirstAction(first);
        } else {
          this.logger.warn("No actions found in quest '{}'", questName);
        }

        currentQuest.setLastActionId(actionId);
      }

      for (Map.Entry<String, List<String>> entry : nextQuestsByQuestName.entrySet()) {
        Quest quest = loadedQuestByName.get(entry.getKey());
        if (entry.getValue() != null) {
          for (String nextQuestName : entry.getValue()) {
            try {
              quest.addNextQuest(loadedQuestByName.get(nextQuestName));
            } catch (InvalidQuestException e) {
              this.logger.warn("Book {} Chapter {} Quest {}: Invalid type at quest '{}'", this.id, chapterName,
                  quest.getName(), nextQuestName);
            }
          }
        }
      }

      for (Map.Entry<String, List<String>> entry : questsToSkipAtStartByQuestName.entrySet()) {
        Quest quest = loadedQuestByName.get(entry.getKey());
        if (entry.getValue() != null) {
          for (String toSkipName : entry.getValue()) {
            Quest toSkip = loadedQuestByName.get(toSkipName);
            if (toSkip != null) {
              quest.addQuestsToSkipAtStart(toSkip);
            } else {
              this.logger.warn("Book {} Chapter {} Quest {}: Unknown quest to skip at start '{}'", this.id,
                  chapterName, quest.getName(), toSkipName);
            }
          }
        }
      }

      for (Map.Entry<String, List<String>> entry : questsToSkipAtEndByQuestName.entrySet()) {
        Quest quest = loadedQuestByName.get(entry.getKey());
        if (entry.getValue() != null) {
          for (String toSkipName : entry.getValue()) {
            Quest toSkip = loadedQuestByName.get(toSkipName);
            if (toSkip != null) {
              quest.addQuestsToSkipAtEnd(toSkip);
            } else {
              this.logger.warn("Book {} Chapter {} Quest {}: Unknown quest to skip at end '{}'", this.id,
                  chapterName, quest.getName(), toSkipName);
            }
          }
        }
      }

      Set<StoryCharacter<?>> characters = new HashSet<>();

      for (String name : characterNames) {
        StoryCharacter<?> character = characterByName.get(name);
        if (character != null) {
          characters.add(character);
        } else {
          this.logger.warn("Book {} Chapter {}: Could not find character '{}'", this.id, chapterName, name);
        }
      }

      StoryChapter previousChapter = currentChapter;

      currentChapter = new StoryChapter(chapterName, chapterDisplayName, chapterEndMessage, diary,
          loadedQuestByName.get(startQuest), playerSizes.stream().map(Long::intValue).toList(),
          maxDeathsByDifficulty, worldName, characters);

      if (previousChapter != null) {
        previousChapter.setNext(chapterName);
        currentChapter.setPrevious(previousChapter.getId());
      }

      chapterByName.put(chapterName, currentChapter);
    }

    StoryBook book = new StoryBook(this.id, bookName, chapterByName, characterByName, itemByName, ambientSounds);
    this.printBookTree(book);
    return book;
  }

  private void printBookTree(StoryBook book) {

    for (StoryChapter chapter : book) {
      LinkedList<String> lines = new LinkedList<>();
      this.print(lines, chapter.getFirstQuest(), "", false, new HashSet<>());
      for (String line : lines) {
        this.logger.info("{}: {}", chapter.getId(), line);
      }
    }
  }

  private void print(LinkedList<String> lines, Quest quest, String prefix, boolean hasNext,
                     Collection<String> visitedQuests) {
    if (visitedQuests.contains(quest.getName())) {
      lines.add(prefix + (hasNext ? "|--" : "\\--") + "& " + quest.getName());
      return;
    }

    visitedQuests.add(quest.getName());

    if (quest instanceof MainQuest) {
      lines.add(prefix + (hasNext ? "|--" : "\\--") + "+ " + quest.getName() + ": "
          + quest.getLastActionId() + " actions" +
          (!quest.questsToSkipAtStart.isEmpty() ? ", start skip:" + String.join(",",
              quest.questsToSkipAtStart.stream().map(Quest::getName).toList()) : "") +
          (!quest.questsToSkipAtEnd.isEmpty() ? ", end skip:" + String.join(",",
              quest.questsToSkipAtEnd.stream().map(Quest::getName).toList()) : ""));
      prefix += (hasNext ? "|  " : "   ");
      boolean hasOptional = !((MainQuest) quest).getNextOptionalQuests().isEmpty();
      for (Iterator<MainQuest> iterator = ((MainQuest) quest).getNextMainQuests().iterator();
           iterator.hasNext(); ) {
        Quest nextMain = iterator.next();
        this.print(lines, nextMain, prefix, iterator.hasNext() || hasOptional,
            visitedQuests);
      }

      for (Iterator<OptionalQuest> iterator = ((MainQuest) quest).getNextOptionalQuests()
          .iterator(); iterator.hasNext(); ) {
        OptionalQuest nextOptional = iterator.next();
        this.print(lines, nextOptional, prefix, iterator.hasNext(), visitedQuests);
      }
    } else {
      lines.add(prefix + (hasNext ? "|-- " : "\\-- ") + quest.getName() + ": "
          + quest.getLastActionId() + " actions" +
          (!quest.questsToSkipAtStart.isEmpty() ? ", start skip:" + String.join(",",
              quest.questsToSkipAtStart.stream().map(Quest::getName).toList()) : "") +
          (!quest.questsToSkipAtEnd.isEmpty() ? ", end skip:" + String.join(",",
              quest.questsToSkipAtEnd.stream().map(Quest::getName).toList()) : ""));
      prefix += (hasNext ? "|  " : "   ");
      for (Iterator<? extends Quest> iterator = quest.getNextQuests().iterator();
           iterator.hasNext(); ) {
        Quest nextOptional = iterator.next();
        this.print(lines, nextOptional, prefix, iterator.hasNext(), visitedQuests);
      }
    }
  }


  private StoryAction getActionFromFile(Quest quest, Toml actionTable, int id)
      throws StoryParseException {

    String actionType = actionTable.getString("action");

    if (actionType == null) {
      actionType = TriggerAction.NAME;
    }

    List<Integer> diaryPages = actionTable.getList("diary");

    StoryAction action = switch (actionType.toLowerCase()) {
      case TalkAction.NAME -> new TalkAction(this, quest, actionTable, id, diaryPages);
      case ItemCollectAction.NAME -> new ItemCollectAction(this, quest, actionTable, id, diaryPages);
      case ItemGiveAction.NAME -> new ItemGiveAction(this, quest, actionTable, id, diaryPages);
      case DelayAction.NAME -> new DelayAction(quest, actionTable, id, diaryPages);
      case ThoughtAction.NAME -> new ThoughtAction(quest, actionTable, id, diaryPages);
      case ClearInventoryAction.NAME -> new ClearInventoryAction(quest, actionTable, id, diaryPages);
      case ItemLootAction.NAME -> new ItemLootAction(this, quest, actionTable, id, diaryPages);
      case SpawnGuardAction.NAME -> new SpawnGuardAction(this, quest, actionTable, id, diaryPages);
      case BlockInteractAction.NAME -> new BlockInteractAction(this, quest, actionTable, id, diaryPages);
      case BlockBreakAction.NAME -> new BlockBreakAction(this, quest, actionTable, id, diaryPages);
      case WeatherAction.NAME -> new WeatherAction(actionTable, id, diaryPages);
      default -> new TriggerAction(quest, actionTable, id, diaryPages);
    };

    if (!(action instanceof TriggeredAction triggeredAction)) {
      return action;
    }

    String triggerType = actionTable.getString("trigger");

    if (triggerType == null) {
      return action;
    }

    switch (triggerType.toLowerCase()) {
      case AreaEvent.NAME -> triggeredAction.setTriggerEvent(
          new AreaEvent<>(triggeredAction, this, actionTable));
      case DropItemAtEvent.NAME -> triggeredAction.setTriggerEvent(
          new DropItemAtEvent<>(quest, triggeredAction, this, actionTable));
      case DropItemEvent.NAME -> triggeredAction.setTriggerEvent(
          new DropItemEvent<>(quest, triggeredAction, this, actionTable));
      case SleepEvent.NAME -> triggeredAction.setTriggerEvent(new SleepEvent<>(triggeredAction));
      case ChatEvent.NAME -> triggeredAction.setTriggerEvent(
          new ChatEvent<>(quest, triggeredAction, actionTable));
      case DelayEvent.NAME -> triggeredAction.setTriggerEvent(
          new DelayEvent<>(quest, triggeredAction, actionTable));
      default -> this.logger.warn("Unknown trigger type: {}", triggerType);
    }

    return action;
  }

  public StoryCharacter<?> getCharacter(String name) throws CharacterNotFoundException {
    StoryCharacter<?> character = this.characterByName.get(name);
    if (character == null) {
      throw new CharacterNotFoundException(name);
    }

    return character;
  }

  public StoryItem getItem(String name) throws ItemNotFoundException {
    StoryItem item = this.itemByName.get(name);
    if (item == null) {
      throw new ItemNotFoundException(name);
    }

    return item;
  }

}
