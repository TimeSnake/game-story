/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin STORY = new Plugin("Story", "GSY");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
