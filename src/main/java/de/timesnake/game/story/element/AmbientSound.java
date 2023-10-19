/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.user.StoryReader;

import java.awt.*;
import java.util.List;

public class AmbientSound {

  private final String name;
  private final String fileName;
  private final AmbientPolygon ambientPolygon;

  public AmbientSound(String name, String fileName, AmbientPolygon ambientPolygon) {
    this.name = name;
    this.fileName = fileName;
    this.ambientPolygon = ambientPolygon;
  }

  public AmbientSound(Toml ambient, String name) throws MissingArgumentException, InvalidArgumentTypeException {
    this.name = name;
    this.fileName = ambient.getString("file");

    if (this.fileName == null) {
      throw new IllegalArgumentException("Can not load file name");
    }

    this.ambientPolygon = new AmbientPolygon(ambient.getTable(AmbientPolygon.NAME));
  }

  public AmbientSound clone(StoryReader reader) {
    return new AmbientSound(this.name, this.fileName, this.ambientPolygon);
  }

  private static class AmbientPolygon {

    public static final String NAME = "polygon";

    private final int minHeight;
    private final int maxHeight;

    private final Polygon polygon;

    public AmbientPolygon(Toml action) throws MissingArgumentException, InvalidArgumentTypeException {
      Long minHeight = action.getLong("min_height");
      if (minHeight == null) {
        throw new MissingArgumentException("min_height");
      }
      this.minHeight = minHeight.intValue();

      Long maxHeight = action.getLong("max_height");
      if (maxHeight == null) {
        throw new MissingArgumentException("max_height");
      }
      this.maxHeight = maxHeight.intValue();

      this.polygon = new Polygon();

      for (Object coords : action.getList("polygon")) {
        if (!(coords instanceof java.util.List<?>)) {
          throw new InvalidArgumentTypeException("the polygon must be an array of x and z tuples");
        }

        java.util.List<Long> coordsList = ((List<Long>) coords);

        if (coordsList.size() != 2) {
          throw new InvalidArgumentTypeException("blocks must be an array of x and z tuples");
        }

        this.polygon.addPoint(coordsList.get(0).intValue(), coordsList.get(1).intValue());
      }
    }

    public boolean contains(ExLocation location) {
      return location.getY() >= this.minHeight && location.getY() < this.maxHeight
          && this.polygon.contains(location.getX(), location.getZ());
    }
  }
}
