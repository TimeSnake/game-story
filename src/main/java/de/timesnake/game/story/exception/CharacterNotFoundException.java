/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.exception;

public class CharacterNotFoundException extends StoryParseException {

  public CharacterNotFoundException(String name) {
    super("Could not find character '" + name + "'");
  }
}
