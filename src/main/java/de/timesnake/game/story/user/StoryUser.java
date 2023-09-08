/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.group.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.ScoreboardManager;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistBuilder;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.database.util.Database;
import de.timesnake.game.story.book.StoryContentBook;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class StoryUser extends User {

  private final UserProgress progress;
  private final Map<Integer, StoryContentBook> contentBookByStoryId = new HashMap<>();
  private final List<StoryUser> selectedUsers = new LinkedList<>();
  private final List<StoryUser> joinedUsers = new LinkedList<>();

  private StoryReader readerGroup;

  private boolean playing = false;
  private boolean spectator = false;

  private Integer selectedBookId;
  private String selectedChapterName;

  public StoryUser(Player player) {
    super(player);

    Tablist tablist = Server.getScoreboardManager()
        .registerGroupTablist(new TablistBuilder(this.getName())
            .groupTypes(DisplayGroup.MAIN_TABLIST_GROUPS)
            .userJoin((e, t) -> {
            })
            .userQuit((e, t) -> {
            }));

    tablist.setHeader("§6Story");
    tablist.setFooter(ScoreboardManager.getDefaultFooter());

    tablist.addEntry(this);

    // TODO this.setTablist(tablist);

    this.progress = new UserProgress(Database.getStory().getUser(this.getUniqueId()));

    for (StoryBook book : StoryServer.getBooks()) {
      this.contentBookByStoryId.put(book.getId(), new StoryContentBook(this.progress, book));
    }
  }

  public StoryReader getReaderGroup() {
    return readerGroup;
  }

  public void setReaderGroup(StoryReader readerGroup) {
    this.readerGroup = readerGroup;
  }

  public void stopStory() {
    if (this.readerGroup != null) {
      this.readerGroup.removeUser(this);
    }

    this.playing = false;
  }

  public void joinStoryHub() {
    this.spectator = false;
    for (User u : Server.getUsers()) {
      u.showUser(this);
    }

    this.setDefault();
    int slot = 0;
    for (StoryContentBook book : this.contentBookByStoryId.values()) {
      this.setItem(slot, book.getItem());
      slot++;
    }
    this.teleport(StoryServer.getBaseWorld().getSpawnLocation());

    if (this.hasPermission("game.story.spectator")) {
      this.setItem(8, UserManager.SPECTATOR_TOOL);
    }

    this.playing = false;
  }

  public void joinSpectator() {
    this.spectator = true;
    this.clearInventory();
    this.setGameMode(GameMode.CREATIVE);
    for (User u : Server.getUsers()) {
      if (!((StoryUser) u).isSpectator()) {
        u.hideUser(this);
      }
    }
    this.setItem(8, UserManager.SPECTATOR_TOOL);
  }

  public boolean isPlaying() {
    return playing;
  }

  public void setPlaying(boolean playing) {
    this.playing = playing;
  }

  public boolean isSpectator() {
    return spectator;
  }

  public void setSpectator(boolean spectator) {
    this.spectator = spectator;
  }

  public Set<String> getBoughtChapters(Integer bookId) {
    return this.progress.getBoughtChaptersByBook().get(bookId);
  }

  public void buyChapter(int bookId, String chapterName) {
    this.removeCoins(StoryServer.PART_PRICE, true);
    this.progress.buyChapter(bookId, chapterName);
  }

  public ExLocation getStoryRespawnLocation() {
    return this.readerGroup.getQuest().getStartLocation();
  }

  public List<StoryUser> getJoinedUsers() {
    return joinedUsers;
  }

  public List<StoryUser> getSelectedUsers() {
    return selectedUsers;
  }

  public void prepareStoryChapter(int bookId, String chapterName) {
    List<StoryUser> users = new LinkedList<>(this.joinedUsers);
    users.add(this);

    this.selectedUsers.clear();
    this.joinedUsers.clear();

    StoryReader readerGroup = new StoryReader(this, users, bookId, chapterName);
    users.forEach(u -> u.setReaderGroup(readerGroup));

    this.selectedBookId = bookId;
    this.selectedChapterName = chapterName;
  }

  public UserProgress getProgress() {
    return progress;
  }
}
