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

package de.timesnake.game.story.user;

import de.timesnake.database.util.story.DbStoryUser;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserProgress {

    public static final String FINISHED_QUEST_NAME = "finished";

    private final DbStoryUser database;

    private final Map<Integer, Set<Integer>> boughtChaptersByBook = new HashMap<>();
    private final Map<Integer, Map<Integer, String>> questsByChaptersByBook = new HashMap<>();

    public UserProgress(DbStoryUser database) {
        this.database = database;

        for (Integer bookId : StoryServer.getBooks().stream().map(StoryBook::getId).toList()) {
            this.boughtChaptersByBook.put(bookId, database.getBoughtParts(bookId));
        }

        for (Integer bookId : database.getChapterIds()) {

            HashMap<Integer, String> questByChapter = new HashMap<>();
            System.out.println(bookId);
            for (Integer chapterId : database.getPartIds(bookId)) {
                System.out.println(chapterId);
                Integer quest = database.getSectionId(bookId, chapterId);
                System.out.println(quest);
                questByChapter.put(chapterId, quest + "");
            }

            this.questsByChaptersByBook.put(bookId, questByChapter);
        }

        if (this.questsByChaptersByBook.isEmpty()) {
            HashMap<Integer, String> questByChapter = new HashMap<>();
            questByChapter.put(1, StoryServer.getBook(1).getChapter(1).getFirstQuest().getName());
            this.questsByChaptersByBook.put(1, questByChapter);
        }
    }

    public Map<Integer, Set<Integer>> getBoughtChaptersByBook() {
        return boughtChaptersByBook;
    }

    public Map<Integer, Map<Integer, String>> getQuestsByChaptersByBook() {
        return questsByChaptersByBook;
    }

    public void buyChapter(int bookId, int chapterId) {
        this.boughtChaptersByBook.computeIfAbsent(bookId, i -> new HashSet<>()).add(chapterId);
    }

    public String getQuest(Integer bookId, Integer chapterId) {
        StoryBook book = StoryServer.getBook(bookId);
        if (book != null) {
            StoryChapter chapter = book.getChapter(chapterId);
            if (chapter != null) {
                Map<Integer, String> currentQuestsByChapter = this.questsByChaptersByBook.get(bookId);
                if (currentQuestsByChapter != null) {
                    String currentQuestName = currentQuestsByChapter.get(chapterId);
                    if (currentQuestName != null) {
                        return currentQuestName;
                    }
                }

                if (this.hasFinishedPreviousPart(bookId, chapterId)) {
                    return chapter.getFirstQuest().getName();
                }
            }
        }
        return null;
    }

    public void finishSection(int bookId, int chapterId, String questName) {
        this.questsByChaptersByBook.computeIfAbsent(bookId, c -> new HashMap<>()).put(chapterId, questName);
    }

    public boolean canPlayPart(int chapterId, int partId) {
        StoryBook book = StoryServer.getBook(chapterId);
        if (book != null) {
            StoryChapter chapter = book.getChapter(partId);
            if (chapter != null) {
                Map<Integer, String> currentQuestByChapter = this.questsByChaptersByBook.get(chapterId);
                if (currentQuestByChapter != null) {
                    String currentQuest = currentQuestByChapter.get(partId);
                    if (currentQuest != null) {
                        return true;
                    }
                }

                return this.hasFinishedPreviousPart(chapterId, partId);
            }
        }
        return false;
    }

    public boolean hasFinishedPreviousPart(int chapterId, int partId) {
        if (partId - 1 <= 0) {
            return this.hasFinishedPreviousChapter(chapterId);
        } else {
            return this.hasFinishedPart(chapterId, partId - 1);
        }
    }

    public boolean hasFinishedPart(int chapterId, int partId) {
        StoryBook book = StoryServer.getBook(chapterId);
        if (book != null) {
            StoryChapter chapter = book.getChapter(partId);
            if (chapter != null) {
                Map<Integer, String> currentQuestByChapter = this.questsByChaptersByBook.get(chapterId);
                if (currentQuestByChapter != null) {
                    String currentQuest = currentQuestByChapter.get(partId);
                    if (currentQuest != null) {
                        return currentQuest.equals(FINISHED_QUEST_NAME);
                    }
                }
            }
        }
        return false;
    }

    public boolean hasFinishedPreviousChapter(int chapterId) {
        if (chapterId - 1 <= 0) {
            return true;
        } else {
            StoryBook chapter = StoryServer.getBook(chapterId);
            if (chapter != null) {
                return this.hasFinishedPart(chapterId, chapter.getLastChapter().getId());
            }
        }
        return false;
    }

}
