/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.listener.EventManager;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.game.story.user.UserManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoryServerManager extends ServerManager implements Listener {

  public static StoryServerManager getInstance() {
    return (StoryServerManager) ServerManager.getInstance();
  }

  private final Logger logger = LogManager.getLogger("story.server");

  private final Map<String, StoryBook> bookById = new HashMap<>();

  private EventManager eventManager;
  private UserManager userManager;
  private StoryFile file;

  private ExWorld baseWorld;

  public void onStoryEnable() {
    this.userManager = new UserManager();

    this.file = new StoryFile(new File("plugins/game-story/story.toml"));

    this.baseWorld = Server.getWorld("world");
    this.baseWorld.setSpawnLocation(0, 70, 0);

    this.baseWorld.restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
    this.baseWorld.restrict(ExWorld.Restriction.NO_PLAYER_DAMAGE, false);
    this.baseWorld.restrict(ExWorld.Restriction.FOOD_CHANGE, true);
    this.baseWorld.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
    this.baseWorld.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
    this.baseWorld.restrict(ExWorld.Restriction.DROP_PICK_ITEM, true);
    this.baseWorld.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
    this.baseWorld.setExceptService(true);
    this.baseWorld.setPVP(false);
    this.baseWorld.restrict(ExWorld.Restriction.NO_PLAYER_DAMAGE, true);
    this.baseWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    this.baseWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    this.baseWorld.setGameRule(GameRule.DISABLE_RAIDS, true);

    this.eventManager = new EventManager();

    // load chapters from file
    for (String id : this.file.getBookIds()) {
      StoryBook book = new StoryBookBuilder(id, Path.of("plugins", "game-story",
          String.valueOf(id))).parseToBook();

      this.bookById.put(id, book);

      this.logger.info("Loaded story book '{}'", id);
    }

    Server.registerListener(this, GameStory.getPlugin());
  }

  @Override
  public StoryUser loadUser(Player player) {
    return new StoryUser(player);
  }

  public StoryBook getBook(String id) {
    return this.bookById.get(id);
  }

  public Collection<StoryBook> getBooks() {
    return this.bookById.values();
  }

  public ExWorld getBaseWorld() {
    return baseWorld;
  }

  public EventManager getEventManager() {
    return eventManager;
  }
}
