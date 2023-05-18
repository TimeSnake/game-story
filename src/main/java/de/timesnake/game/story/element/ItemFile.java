/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.file.ExToml;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ItemFile {

  private static final String ITEM = "item";

  private final ExToml file;

  public ItemFile(File file) {
    this.file = new ExToml(file);
  }

  public Map<String, Toml> getItemTables() {
    Map<String, Toml> map = new HashMap<>();
    if (this.file.containsTable(ITEM)) {
      for (String name : this.file.getTable(ITEM).toMap().keySet()) {
        map.put(name, this.file.getTable(ITEM).getTable(name));
      }
    }
    return map;
  }
}
