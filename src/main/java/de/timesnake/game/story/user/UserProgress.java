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

package de.timesnake.game.story.user;

import de.timesnake.database.util.story.DbStoryUser;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserProgress {

    public static final String FINISHED_QUEST_NAME = "FINISHED";

    private final DbStoryUser database;

    private final Map<Integer, Set<String>> boughtChaptersByBook = new HashMap<>();
    private final Map<Integer, Map<String, String>> questByChapterByBook = new HashMap<>();

    public UserProgress(DbStoryUser database) {
        this.database = database;

        for (Integer bookId : StoryServer.getBooks().stream().map(StoryBook::getId).toList()) {
            this.boughtChaptersByBook.put(bookId, database.getBoughtChapters(bookId).stream().map(String::valueOf)
                    .collect(Collectors.toSet()));
        }

        for (Integer bookId : database.getChapterIds()) {

            HashMap<String, String> questByChapter = new HashMap<>();
            for (String chapterName : database.getChapterIds(bookId)) {
                String quest = database.getQuestName(bookId, chapterName);
                questByChapter.put(chapterName, quest);
            }

            this.questByChapterByBook.put(bookId, questByChapter);
        }
    }

    public Map<Integer, Set<String>> getBoughtChaptersByBook() {
        return boughtChaptersByBook;
    }

    public Map<Integer, Map<String, String>> getQuestByChapterByBook() {
        return questByChapterByBook;
    }

    public void buyChapter(int bookId, String chapterName) {
        this.boughtChaptersByBook.computeIfAbsent(bookId, i -> new HashSet<>()).add(chapterName);
        this.database.addBoughtChapter(bookId, chapterName);
    }

    public String getQuest(Integer bookId, String chapterName) {
        Map<String, String> currentQuestsByChapter = this.questByChapterByBook.get(bookId);
        if (currentQuestsByChapter != null) {
            return currentQuestsByChapter.get(chapterName);
        }
        return null;
    }

    public void saveQuest(int bookId, String chapterName, String questName) {
        this.questByChapterByBook.computeIfAbsent(bookId, c -> new HashMap<>()).put(chapterName, questName);
    }

    public void saveChapter(int bookId, String chapterName) {
        this.questByChapterByBook.computeIfAbsent(bookId, c -> new HashMap<>()).put(chapterName, FINISHED_QUEST_NAME);
    }

    public boolean canPlayChapter(int bookId, String chapterName) {
        StoryBook book = StoryServer.getBook(bookId);
        if (book != null) {
            // first chapter can always be played
            if (book.getFirstChapter().getName().equals(chapterName)) {
                return true;
            }

            StoryChapter chapter = book.getChapter(chapterName);

            if (chapter != null) {
                Map<String, String> currentQuestByChapter = this.questByChapterByBook.get(bookId);

                if (currentQuestByChapter != null) {
                    String currentQuest = currentQuestByChapter.get(chapterName);

                    // user already started this chapter
                    if (currentQuest != null) {
                        return true;
                    }
                }

                // check if previous is finished
                return this.hasFinishedPreviousChapter(bookId, chapterName);
            }
        }
        return false;
    }

    public boolean hasFinishedPreviousChapter(int book, String chapterName) {
        StoryChapter previousChapter = StoryServer.getBook(book).getPreviousChapter(chapterName);

        // no previous chapter exists
        if (previousChapter == null) {
            return true;
        } else {
            return this.hasFinishedChapter(book, previousChapter.getName());
        }
    }

    public boolean hasFinishedChapter(int bookId, String chapterName) {
        StoryBook book = StoryServer.getBook(bookId);
        if (book != null) {
            StoryChapter chapter = book.getChapter(chapterName);
            if (chapter != null) {
                Map<String, String> currentQuestByChapter = this.questByChapterByBook.get(bookId);
                if (currentQuestByChapter != null) {
                    String currentQuest = currentQuestByChapter.get(chapterName);
                    if (currentQuest != null) {
                        return currentQuest.equals(FINISHED_QUEST_NAME);
                    }
                }
            }
        }
        return false;
    }

}
