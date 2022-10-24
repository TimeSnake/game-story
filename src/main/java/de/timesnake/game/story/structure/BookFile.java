/*
 * game-story.main
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
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.*;
import de.timesnake.game.story.server.StoryServer;

import java.io.File;
import java.util.*;

public class BookFile {

    private static final String NAME = "name";

    private static final String END_MESSAGE = "end_message";

    private static final String TRIGGER_TYPE = "trigger";
    private static final String ACTION_TYPE = "action";

    private static final String DIARY = "diary";

    private final int id;
    private final ExToml file;

    public BookFile(int id, File file) {
        this.id = id;
        this.file = new ExToml(file);
    }

    public StoryBook parseToBook() {
        Server.printText(Plugin.STORY, "Loading book " + this.id);
        String bookName = this.file.getString(NAME);

        SortedMap<Integer, StoryChapter> chapterById = new TreeMap<>();

        for (int chapterId = 1; this.file.containsTable("chapter_" + chapterId); chapterId++) {
            Toml chapter = this.file.getTable("chapter_" + chapterId);

            String chapterName = chapter.getString(NAME);
            String chapterEndMessage = chapter.getString(END_MESSAGE);
            List<Long> playerSizes = chapter.getList("players");
            String worldName = chapter.getString("world");

            Toml diaryTable = chapter.getTable("diary");
            Diary diary = new Diary(diaryTable, chapterId);

            Set<String> characterNames = new HashSet<>();

            String startQuest = chapter.getString("start_quest");

            Set<String> questsToLoad = new HashSet<>();
            Map<String, Quest> loadedQuestByName = new HashMap<>();
            Map<String, List<String>> nextQuestsByQuestName = new HashMap<>();

            questsToLoad.add(startQuest);

            Server.printText(Plugin.STORY, "Start-Quest: " + startQuest, "Book " + this.id, "Chapter " + chapterId);

            while (!questsToLoad.isEmpty()) {
                String questName = questsToLoad.iterator().next();
                questsToLoad.remove(questName);

                if (loadedQuestByName.containsKey(questName)) {
                    continue;
                }

                Toml quest = chapter.getTable("quest_" + questName);

                Quest.Type type = Quest.Type.valueOf(quest.getString("type").toUpperCase());

                StoryAction first = null;
                StoryAction previous = null;
                StoryAction current;

                StringBuilder sb = new StringBuilder("Actions: ");

                for (int actionId = 1; quest.contains("action_" + actionId); actionId++) {
                    Toml action = quest.getTable("action_" + actionId);
                    try {
                        current = this.getActionFromFile(action, actionId);
                    } catch (CharacterNotFoundException | ItemNotFoundException | UnknownLocationException
                             | UnknownGuardTypeException e) {
                        Server.printWarning(Plugin.STORY, e.getMessage(), "Book " + this.id,
                                "Chapter " + chapterId, "Quest " + questName, "Action " + actionId);
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

                Server.printText(Plugin.STORY, sb.toString(), "Book " + this.id, "Chapter " + chapterId, "Quest " + questName);
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
                try {
                    characters.add(StoryServer.getCharater(name));
                } catch (CharacterNotFoundException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Book " + this.id, "Chapter " + chapterId);

                }
            }

            chapterById.put(chapterId, new StoryChapter(chapterId, chapterName, chapterEndMessage, diary,
                    loadedQuestByName.get(startQuest), playerSizes, worldName, characters));

            Server.printText(Plugin.STORY, "Loaded chapter " + chapterId, "Book " + this.id);
        }

        return new StoryBook(this.id, bookName, chapterById);
    }

    private StoryAction getActionFromFile(Toml actionTable, int id)
            throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException, UnknownGuardTypeException {

        String actionType = actionTable.getString("action");

        if (actionType == null) {
            actionType = TriggerAction.NAME;
        }

        List<Integer> diaryPages = actionTable.getList("diary");

        StoryAction action = switch (actionType.toLowerCase()) {
            case TalkAction.NAME -> new TalkAction(actionTable, id, diaryPages);
            case ItemSearchAction.NAME -> new ItemSearchAction(actionTable, id, diaryPages);
            case ItemGiveAction.NAME -> new ItemGiveAction(actionTable, id, diaryPages);
            case DelayAction.NAME -> new DelayAction(actionTable, id, diaryPages);
            case ThoughtAction.NAME -> new ThoughtAction(actionTable, id, diaryPages);
            case ClearInventoryAction.NAME -> new ClearInventoryAction(actionTable, id, diaryPages);
            case ItemLootAction.NAME -> new ItemLootAction(actionTable, id, diaryPages);
            case TriggerAction.NAME -> new TriggerAction(actionTable, id, diaryPages);
            case SpawnGuardAction.NAME -> new SpawnGuardAction(actionTable, id, diaryPages);
            case BlockInteractAction.NAME -> new BlockInteractAction(actionTable, id, diaryPages);
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
            case AreaEvent.NAME -> triggeredAction.setTriggerEvent(new AreaEvent<>(triggeredAction, actionTable));
            case DropItemAtEvent.NAME ->
                    triggeredAction.setTriggerEvent(new DropItemAtEvent<>(triggeredAction, actionTable));
            case DropItemEvent.NAME ->
                    triggeredAction.setTriggerEvent(new DropItemEvent<>(triggeredAction, actionTable));
            case SleepEvent.NAME -> triggeredAction.setTriggerEvent(new SleepEvent<>(triggeredAction));
            case ChatEvent.NAME -> triggeredAction.setTriggerEvent(new ChatEvent<>(triggeredAction, actionTable));
            default -> Server.printWarning(Plugin.STORY, "Unknown trigger type: " + triggerType);
        }

        return action;
    }
}
