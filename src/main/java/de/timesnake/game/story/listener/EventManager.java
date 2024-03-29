/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.listener;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.UserBlockBreakEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.game.story.main.GameStory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager implements Listener {

  private final Logger logger = LogManager.getLogger("story.event-manager");

  private final ConcurrentHashMap<StoryEventListener, Map<Class<? extends Event>, Method>> methodByEventByListener = new ConcurrentHashMap<>();

  public EventManager() {
    Server.registerListener(this, GameStory.getPlugin());
  }

  public void registerListeners(StoryEventListener listener) {
    Class<? extends StoryEventListener> clazz = listener.getClass();
    Arrays.stream(clazz.getMethods()).filter(m -> m.isAnnotationPresent(StoryEvent.class))
        .forEach(method -> {
          Class<?>[] parameters = method.getParameterTypes();
          if (parameters.length == 1) {
            this.methodByEventByListener.computeIfAbsent(listener, e -> new HashMap<>()).put((Class<? extends Event>) parameters[0], method);
          } else {
            this.logger.warn("Failed to add story event method '{}' in class '{}'", method.getName(), clazz.getName());
          }
        });
  }

  public void unregisterListeners(StoryEventListener listener) {
    this.methodByEventByListener.remove(listener);
  }

  @EventHandler
  public void onBlockBreak(UserBlockBreakEvent e) {
    this.handleEvent(e);
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {
    this.handleEvent(e);
  }

  @EventHandler
  public void onUserMove(UserMoveEvent e) {
    this.handleEvent(e);
  }

  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent e) {
    this.handleEvent(e);
  }

  @EventHandler
  public void onPlayerSleep(PlayerBedEnterEvent e) {
    this.handleEvent(e);
  }

  @EventHandler
  public void onUserDropItem(UserDropItemEvent e) {
    this.handleEvent(e);
  }

  private void handleEvent(Event event) {
    this.methodByEventByListener.forEach((k, v) -> {
      Method method = v.get(event.getClass());
      if (method != null) {
        try {
          method.setAccessible(true);
          v.get(event.getClass()).invoke(k, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
          this.logger.warn("Failed to invoke story event method '{}' in class '{}': {}: {}",
              method.getName(), k.getClass().getName(), e.getClass().getSimpleName(), e.getCause().getMessage());
        }
      }
    });
  }
}
