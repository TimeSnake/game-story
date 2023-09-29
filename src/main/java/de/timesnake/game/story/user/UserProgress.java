/*
 * Copyright (C) 2023 timesnake
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

  public static final String FINISHED_QUEST_NAME = "FINISHED";

  private final DbStoryUser database;
  private final Map<String, Set<String>> boughtChaptersByBook = new HashMap<>();
  private final Map<String, Map<String, String>> questByChapterByBook = new HashMap<>();

  public UserProgress(DbStoryUser database) {
    this.database = database;

    for (String bookId : StoryServer.getBooks().stream().map(StoryBook::getId).toList()) {
      this.boughtChaptersByBook.put(bookId, database.getBoughtChapters(bookId));
    }

    for (String bookId : database.getBookIds()) {
      HashMap<String, String> questByChapter = new HashMap<>();
      for (String chapterName : database.getBookIds(bookId)) {
        String quest = database.getQuestName(bookId, chapterName);
        questByChapter.put(chapterName, quest);
      }

      this.questByChapterByBook.put(bookId, questByChapter);
    }

  }

  public Map<String, Set<String>> getBoughtChaptersByBook() {
    return boughtChaptersByBook;
  }

  public Map<String, Map<String, String>> getQuestByChapterByBook() {
    return questByChapterByBook;
  }

  public void buyChapter(String bookId, String chapterId) {
    this.boughtChaptersByBook.computeIfAbsent(bookId, i -> new HashSet<>()).add(chapterId);
    this.database.addBoughtChapter(bookId, chapterId);
  }

  public String getQuest(String bookId, String chapterId) {
    Map<String, String> currentQuestsByChapter = this.questByChapterByBook.get(bookId);
    if (currentQuestsByChapter != null) {
      return currentQuestsByChapter.get(chapterId);
    }
    return null;
  }

  public void saveQuest(String bookId, String chapterId, String questName) {
    this.questByChapterByBook.computeIfAbsent(bookId, c -> new HashMap<>())
        .put(chapterId, questName);
  }

  public void saveChapter(String bookId, String chapterId) {
    this.questByChapterByBook.computeIfAbsent(bookId, c -> new HashMap<>())
        .put(chapterId, FINISHED_QUEST_NAME);
  }

  public boolean canPlayChapter(String bookId, String chapterId) {
    StoryBook book = StoryServer.getBook(bookId);
    if (book != null) {
      // first chapter can always be played
      if (book.getFirstChapter().getId().equals(chapterId)) {
        return true;
      }

      StoryChapter chapter = book.getChapter(chapterId);

      if (chapter != null) {
        Map<String, String> currentQuestByChapter = this.questByChapterByBook.get(bookId);

        if (currentQuestByChapter != null) {
          String currentQuest = currentQuestByChapter.get(chapterId);

          // user already started this chapter
          if (currentQuest != null) {
            return true;
          }
        }

        // check if previous is finished
        return this.hasFinishedPreviousChapter(bookId, chapterId);
      }
    }
    return false;
  }

  public boolean hasFinishedPreviousChapter(String book, String chapterId) {
    StoryChapter previousChapter = StoryServer.getBook(book).getPreviousChapter(chapterId);

    // no previous chapter exists
    if (previousChapter == null) {
      return true;
    } else {
      return this.hasFinishedChapter(book, previousChapter.getId());
    }
  }

  public boolean hasFinishedChapter(String bookId, String chapterId) {
    StoryBook book = StoryServer.getBook(bookId);
    if (book != null) {
      StoryChapter chapter = book.getChapter(chapterId);
      if (chapter != null) {
        Map<String, String> currentQuestByChapter = this.questByChapterByBook.get(bookId);
        if (currentQuestByChapter != null) {
          String currentQuest = currentQuestByChapter.get(chapterId);
          if (currentQuest != null) {
            return currentQuest.equals(FINISHED_QUEST_NAME);
          }
        }
      }
    }
    return false;
  }

}
