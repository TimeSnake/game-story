/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.file.ExToml;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CharacterFile {

  private static final String CHARACTER = "character";

  private final ExToml file;

  public CharacterFile(File file) {
    this.file = new ExToml(file);
  }

  public Map<String, Toml> getCharacterTables() {
    Map<String, Toml> map = new HashMap<>();
    for (String name : this.file.getTable(CHARACTER).toMap().keySet()) {
      map.put(name, this.file.getTable(CHARACTER).getTable(name));
    }
    return map;
  }
}
