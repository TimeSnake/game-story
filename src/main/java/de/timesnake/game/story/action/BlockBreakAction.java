/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.CancelPriority;
import de.timesnake.basic.bukkit.util.user.event.UserBlockBreakEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class BlockBreakAction extends StoryAction {

  public static final String NAME = "block_break";

  private final List<Material> materials;
  private final BreakArea breakArea;

  protected BlockBreakAction(int id, StoryAction next, BreakArea breakArea,
      List<Material> materials) {
    super(id, next);
    this.breakArea = breakArea;
    this.materials = materials;
  }

  public BlockBreakAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id,
      List<Integer> diaryPages)
      throws MissingArgumentException, InvalidArgumentTypeException {
    super(id, diaryPages);

    if (action.contains(BreakBlock.NAME)) {
      this.breakArea = new BreakBlock(action);
    } else if (action.contains(BreakPolygon.NAME)) {
      this.breakArea = new BreakPolygon(action);
    } else {
      throw new MissingArgumentException("break area");
    }

    List<String> materialNames = action.getList("materials");

    if (materialNames == null) {
      throw new MissingArgumentException("materials");
    }

    this.materials = new LinkedList<>();
    for (String materialName : materialNames) {
      Material material = Material.getMaterial(materialName.toUpperCase());

      if (material == null) {
        throw new InvalidArgumentTypeException("invalid material '" + materialName + "'");
      }
      this.materials.add(material);
    }

  }

  @Override
  public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
      StoryChapter chapter) {
    return new BlockBreakAction(this.id, clonedNext, breakArea, materials);
  }

  @Override
  public void start() {
    super.start();
    super.startNext();
  }

  @Override
  public void stop() {
    super.stop();
    StoryServer.getEventManager().registerListeners(this);
  }

  @StoryEvent
  public void onUserBreakBlock(UserBlockBreakEvent e) {
    if (!this.isActive()) {
      if (!this.getNext().isActive()) {
        StoryServer.getEventManager().unregisterListeners(this);
        return;
      }
    }

    StoryUser user = ((StoryUser) e.getUser());

    if (!this.reader.containsUser(user)) {
      return;
    }

    if (!Server.getWorld(e.getBlock().getWorld()).equals(this.reader.getWorld())) {
      return;
    }

    if (!this.breakArea.contains(ExLocation.fromLocation(e.getBlock().getLocation()))) {
      return;
    }

    if (!this.materials.isEmpty() && !this.materials.contains(e.getBlock().getType())) {
      return;
    }

    e.setCancelled(CancelPriority.HIGH, false);
    e.setDropItems(true);
  }

  public interface BreakArea {

    boolean contains(ExLocation location);

  }

  private static class BreakPolygon implements BreakArea {

    public static final String NAME = "polygon";

    private final int minHeight;
    private final int maxHeight;

    private final Polygon polygon;

    public BreakPolygon(Toml action) throws MissingArgumentException, InvalidArgumentTypeException {

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
        if (!(coords instanceof List<?>)) {
          throw new InvalidArgumentTypeException("the polygon must be an array of x and z tuples");
        }

        List<Long> coordsList = ((List<Long>) coords);

        if (coordsList.size() != 2) {
          throw new InvalidArgumentTypeException("blocks must be an array of x and z tuples");
        }

        this.polygon.addPoint(coordsList.get(0).intValue(), coordsList.get(1).intValue());
      }
    }

    @Override
    public boolean contains(ExLocation location) {
      return location.getY() >= this.minHeight && location.getY() < this.maxHeight
          && this.polygon.contains(location.getX(), location.getZ());
    }
  }

  private static class BreakBlock implements BreakArea {

    public static final String NAME = "block";

    private final ExLocation location;

    public BreakBlock(Toml action) throws MissingArgumentException {
      this.location = ExLocation.fromList(action.getList(NAME));

      if (this.location == null) {
        throw new MissingArgumentException("block");
      }
    }

    @Override
    public boolean contains(ExLocation location) {
      return this.location.getBlockX() == location.getBlockX()
          && this.location.getBlockY() == location.getBlockY()
          && this.location.getBlockZ() == location.getBlockZ();
    }
  }
}
