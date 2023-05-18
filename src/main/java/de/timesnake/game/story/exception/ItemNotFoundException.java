/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.exception;

public class ItemNotFoundException extends StoryParseException {

  public ItemNotFoundException(String name) {
    super("Could not find item '" + name + "'");
  }
}
