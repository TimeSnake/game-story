/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.structure.StoryBookBuilder;
import java.util.List;

public abstract class RadiusAction extends LocationAction {

  protected final Double radius;

  public RadiusAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
      Double radius) {
    super(id, next, location, character);
    this.radius = radius;
  }

  public RadiusAction(StoryBookBuilder bookBuilder, Toml action, int id, List<Integer> diaryPages)
      throws StoryParseException {
    super(bookBuilder, action, id, diaryPages);
    Double radius;
    try {
      radius = action.getDouble(RADIUS);
    } catch (ClassCastException e) {
      radius = action.getLong(RADIUS).doubleValue();
    }

    if (radius == null) {
      throw new MissingArgumentException("radius");
    }

    this.radius = radius;
  }
}
