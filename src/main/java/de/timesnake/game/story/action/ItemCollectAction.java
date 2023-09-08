/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.ItemEntity;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemCollectAction extends LocationAction {

  public static final String NAME = "item_collect";

  private static final String ANGLE = "angle";

  private final float itemAngle;
  private StoryItem item;
  private Material material;
  private ItemEntity entity;

  public ItemCollectAction(int id, StoryAction next, ExLocation location,
                           StoryCharacter<?> character,
                           StoryItem item, Material material, float itemAngle) {
    super(id, next, location, character);
    this.item = item;
    this.itemAngle = itemAngle;
    this.material = material;
  }

  public ItemCollectAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id,
                           List<Integer> diaryPages)
      throws StoryParseException {
    super(bookBuilder, action, id, diaryPages);

    if (action.contains("item")) {
      String itemName = action.getString("item");
      if (itemName == null) {
        throw new MissingArgumentException("item");
      }
      this.item = bookBuilder.getItem(itemName);
    } else if (action.contains("material")) {
      String materialName = action.getString("material");
      this.material = Material.getMaterial(materialName.toUpperCase());
      if (material == null) {
        throw new InvalidArgumentTypeException("invalid material '" + materialName + "'");
      }
    } else {
      throw new MissingArgumentException("item", "material");
    }

    float itemAngle;
    try {
      itemAngle = action.getDouble(ANGLE).floatValue();
    } catch (ClassCastException e) {
      itemAngle = action.getLong(ANGLE).floatValue();
    }

    this.itemAngle = itemAngle;
  }

  @Override
  public ItemCollectAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
                                 StoryChapter chapter) {
    return new ItemCollectAction(this.id, clonedNext,
        this.location.clone().setExWorld(chapter.getWorld()),
        this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
        this.item != null ? this.item.clone(reader) : null, this.material, this.itemAngle);
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    this.collectItem(user);
    this.startNext();
  }

  @Override
  public void stop() {
    super.stop();
    this.despawnEntities();
  }

  private void collectItem(StoryUser user) {
    if (this.item != null) {
      user.addItem(this.item.getItem());
    } else {
      user.addItem(new ItemStack(this.material));
    }
    this.despawnEntities();
  }

  @Override
  public void spawnEntities() {
    this.entity = new ItemEntity(this.location, this.item != null ? this.item.getItem() : new ItemStack(this.material),
        this.itemAngle, 0, 0, false);

    Server.getEntityManager().registerEntity(this.entity, this.reader.getUsers());
  }

  @Override
  public void despawnEntities() {
    if (this.entity != null) {
      Server.getEntityManager().unregisterEntity(this.entity);
    }
  }
}
