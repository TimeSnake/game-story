/*
 * timesnake.game-story.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExToml;
import de.timesnake.game.story.action.*;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.CharacterFile;
import de.timesnake.game.story.element.ItemFile;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.event.*;
import de.timesnake.game.story.exception.*;

import java.nio.file.Path;
import java.util.*;

public class StoryBookBuilder {

    private static final String NAME = "name";

    private static final String END_MESSAGE = "end_message";

    private final int id;
    private final Path folder;
    private final ExToml bookToml;

    private final Map<String, StoryCharacter<?>> characterByName = new HashMap<>();
    private final Map<String, StoryItem> itemByName = new HashMap<>();

    public StoryBookBuilder(int id, Path folder) {
        this.id = id;
        this.folder = folder;
        this.bookToml = new ExToml(folder.resolve("book.toml").toFile());
    }

    public StoryBook parseToBook() {
        Server.printText(Plugin.STORY, "Loading book " + this.id);

        String bookName = this.bookToml.getString(NAME);

        // load characters
        CharacterFile characterFile = new CharacterFile(folder.resolve("characters.toml").toFile());

        for (Map.Entry<String, Toml> entry : characterFile.getCharacterTables().entrySet()) {
            String characterName = entry.getKey();
            StoryCharacter<?> character = null;
            try {
                character = StoryCharacter.initCharacter(characterName, entry.getValue());
            } catch (MissingArgumentException | InvalidArgumentTypeException e) {
                Server.printText(Plugin.STORY, e.getMessage(), "Character");
            }

            if (character != null) {
                if (characterByName.containsKey(characterName)) {
                    Server.printWarning(Plugin.STORY, "Duplicate character name '" +
                            characterName + "'", "Character");
                    continue;
                }
                characterByName.put(characterName, character);
                Server.printText(Plugin.STORY, "Loaded character " + characterName);
            } else {
                Server.printWarning(Plugin.STORY, "Could not load type of character '" +
                        characterName + "'", "Character");
            }

        }

        // load items
        ItemFile itemFile = new ItemFile(folder.resolve("items.toml").toFile());

        for (Map.Entry<String, Toml> entry : itemFile.getItemTables().entrySet()) {
            String itemName = entry.getKey();
            itemByName.put(itemName, new StoryItem(entry.getValue(), itemName));

            Server.printText(Plugin.STORY, "Loaded item '" + itemName + "'");
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
                Server.printText(Plugin.STORY, "Missing argument 'players'", "Book " + this.id,
                        "Chapter " + chapterName);
                playerSizes = List.of(1L);
            }

            String worldName = chapter.getString("world");

            Toml diaryTable = chapter.getTable("diary");
            Diary diary = new Diary(diaryTable, chapterName);

            Set<String> characterNames = new HashSet<>();

            String startQuest = chapter.getString("start_quest");

            Set<String> questsToLoad = new HashSet<>();
            Map<String, Quest> loadedQuestByName = new HashMap<>();
            Map<String, List<String>> nextQuestsByQuestName = new HashMap<>();

            questsToLoad.add(startQuest);

            Server.printText(Plugin.STORY, "Start-Quest: " + startQuest, "Book " + this.id,
                    "Chapter " + chapterName);

            while (!questsToLoad.isEmpty()) {
                String questName = questsToLoad.iterator().next();
                questsToLoad.remove(questName);

                if (loadedQuestByName.containsKey(questName)) {
                    continue;
                }

                Toml quest = chapter.getTable("quest." + questName);

                Quest.Type type = Quest.Type.valueOf(quest.getString("type").toUpperCase());

                StoryAction first = null;
                StoryAction previous = null;
                StoryAction current;

                StringBuilder sb = new StringBuilder("Actions: ");

                for (int actionId = 1; quest.contains(String.valueOf(actionId)); actionId++) {
                    Toml action = quest.getTable(String.valueOf(actionId));
                    try {
                        current = this.getActionFromFile(action, actionId);
                    } catch (CharacterNotFoundException | ItemNotFoundException | UnknownLocationException
                             | UnknownGuardTypeException | MissingArgumentException | InvalidArgumentTypeException e) {
                        Server.printWarning(Plugin.STORY, e.getMessage(), "Book " + this.id,
                                "Chapter " + chapterName, "Quest " + questName, "Action " + actionId);
                        continue;
                    }

                    sb.append(actionId).append(", ");

                    characterNames.addAll(current.getCharacterNames());

                    if (actionId == 1) {
                        first = current;
                    }

                    if (previous != null) {
                        previous.setNext(current);
                    }

                    previous = current;
                }

                if (type.equals(Quest.Type.MAIN)) {
                    loadedQuestByName.put(questName, new MainQuest(quest, questName, first));
                } else {
                    loadedQuestByName.put(questName, new OptionalQuest(quest, questName, first));
                }

                List<String> next = quest.getList("next");
                nextQuestsByQuestName.put(questName, next);

                if (next != null) {
                    questsToLoad.addAll(next);
                }

                Server.printText(Plugin.STORY, sb.toString(), "Book " + this.id, "Chapter " + chapterName,
                        "Quest " + questName);
            }

            for (Map.Entry<String, List<String>> entry : nextQuestsByQuestName.entrySet()) {
                Quest quest = loadedQuestByName.get(entry.getKey());
                if (entry.getValue() != null) {
                    for (String nextQuestName : entry.getValue()) {
                        quest.addNextQuest(loadedQuestByName.get(nextQuestName));
                    }
                }
            }

            Set<StoryCharacter<?>> characters = new HashSet<>();

            for (String name : characterNames) {
                StoryCharacter<?> character = characterByName.get(name);
                if (character != null) {
                    characters.add(character);
                } else {
                    Server.printWarning(Plugin.STORY, "Could not find character '" + name + "'",
                            "Book " + this.id, "Chapter " + chapterName);
                }
            }

            StoryChapter previousChapter = currentChapter;

            currentChapter = new StoryChapter(chapterName, chapterDisplayName, chapterEndMessage, diary,
                    loadedQuestByName.get(startQuest), playerSizes.stream().map(Long::intValue).toList(), worldName, characters);

            if (previousChapter != null) {
                previousChapter.setNext(chapterName);
                currentChapter.setPrevious(previousChapter.getName());
            }

            chapterByName.put(chapterName, currentChapter);

            Server.printText(Plugin.STORY, "Loaded chapter " + chapterName, "Book " + this.id);
        }

        return new StoryBook(this.id, bookName, chapterByName, characterByName, itemByName);
    }

    private StoryAction getActionFromFile(Toml actionTable, int id)
            throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException,
            UnknownGuardTypeException, MissingArgumentException, InvalidArgumentTypeException {

        String actionType = actionTable.getString("action");

        if (actionType == null) {
            actionType = TriggerAction.NAME;
        }

        List<Integer> diaryPages = actionTable.getList("diary");

        StoryAction action = switch (actionType.toLowerCase()) {
            case TalkAction.NAME -> new TalkAction(this, actionTable, id, diaryPages);
            case ItemCollectAction.NAME -> new ItemCollectAction(this, actionTable, id, diaryPages);
            case ItemGiveAction.NAME -> new ItemGiveAction(this, actionTable, id, diaryPages);
            case DelayAction.NAME -> new DelayAction(actionTable, id, diaryPages);
            case ThoughtAction.NAME -> new ThoughtAction(actionTable, id, diaryPages);
            case ClearInventoryAction.NAME -> new ClearInventoryAction(actionTable, id, diaryPages);
            case ItemLootAction.NAME -> new ItemLootAction(this, actionTable, id, diaryPages);
            case SpawnGuardAction.NAME -> new SpawnGuardAction(this, actionTable, id, diaryPages);
            case BlockInteractAction.NAME -> new BlockInteractAction(this, actionTable, id, diaryPages);
            case BlockBreakAction.NAME -> new BlockBreakAction(this, actionTable, id, diaryPages);
            default -> new TriggerAction(actionTable, id, diaryPages);
        };

        if (!(action instanceof TriggeredAction triggeredAction)) {
            return action;
        }

        String triggerType = actionTable.getString("trigger");

        if (triggerType == null) {
            return action;
        }

        switch (triggerType.toLowerCase()) {
            case AreaEvent.NAME -> triggeredAction.setTriggerEvent(new AreaEvent<>(triggeredAction, this, actionTable));
            case DropItemAtEvent.NAME ->
                    triggeredAction.setTriggerEvent(new DropItemAtEvent<>(triggeredAction, this, actionTable));
            case DropItemEvent.NAME ->
                    triggeredAction.setTriggerEvent(new DropItemEvent<>(triggeredAction, this, actionTable));
            case SleepEvent.NAME -> triggeredAction.setTriggerEvent(new SleepEvent<>(triggeredAction));
            case ChatEvent.NAME -> triggeredAction.setTriggerEvent(new ChatEvent<>(triggeredAction, actionTable));
            default -> Server.printWarning(Plugin.STORY, "Unknown trigger type: " + triggerType);
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
