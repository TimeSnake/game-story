/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.server;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.listener.EventManager;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.library.chat.Plugin;

import java.util.Collection;

public class StoryServer {

  public static final Plugin PLUGIN = new Plugin("Story", "GSY");

  public static final int PART_PRICE = 200;

  public static StoryBook getBook(String id) {
    return server.getBook(id);
  }

  public static Collection<StoryBook> getBooks() {
    return server.getBooks();
  }

  public static ExWorld getBaseWorld() {
    return server.getBaseWorld();
  }

  public static EventManager getEventManager() {
    return server.getEventManager();
  }

  private static final StoryServerManager server = StoryServerManager.getInstance();
}
