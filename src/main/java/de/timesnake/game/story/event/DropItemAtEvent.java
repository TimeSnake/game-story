/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import java.util.function.Supplier;
import org.bukkit.Material;

public class DropItemAtEvent<Action extends TriggeredAction> extends LocationEvent<Action> {

  public static final String NAME = "drop_at";

  private final Supplier<Integer> amount;
  private StoryItem item;
  private Material material;
  private boolean dropped = false;

  protected DropItemAtEvent(ExLocation location, StoryCharacter<?> character, StoryItem item,
      Material material,
      Supplier<Integer> amount) {
    super(location, character);
    this.item = item;
    this.material = material;
    this.amount = amount;
  }

  public DropItemAtEvent(Quest quest, Action action, StoryBookBuilder bookBuilder, Toml trigger)
      throws StoryParseException {
    super(action, bookBuilder, trigger);

    if (trigger.contains("item")) {
      String itemName = trigger.getString("item");
      if (itemName == null) {
        throw new MissingArgumentException("item");
      }
      this.item = bookBuilder.getItem(itemName);
    } else if (trigger.contains("material")) {
      String materialName = trigger.getString("material");
      this.material = Material.getMaterial(materialName.toUpperCase());
      if (material == null) {
        throw new InvalidArgumentTypeException("invalid material '" + materialName + "'");
      }
    } else {
      throw new MissingArgumentException("item", "material");
    }

    Supplier<Integer> amount;
    try {
      amount = quest.parseAdvancedInt(trigger, "amount");
    } catch (MissingArgumentException e) {
      amount = () -> 1;
    }
    this.amount = amount;
  }

  @StoryEvent
  public void onUserDropItem(UserDropItemEvent e) {

    StoryUser user = (StoryUser) e.getUser();

    if (!this.action.getReader().containsUser(user)) {
      return;
    }

    if (!this.action.isActive()) {
      return;
    }

    if (e.getItemStack().getAmount() < this.amount.get()) {
      return;
    }

    if (this.item != null && !this.item.getItem()
        .equals(ExItemStack.getItem(e.getItemStack(), false))) {
      return;
    }

    if (this.material != null && !this.material.equals(e.getItemStack().getType())) {
      return;
    }

    if (!this.location.getExWorld().equals(user.getExWorld())) {
      return;
    }

    if (this.dropped) {
      return;
    }

    this.dropped = true;

    Server.runTaskLaterSynchrony(() -> {
      if (this.location.distanceSquared(e.getItemDrop().getLocation()) < 2) {
        e.getItemDrop().remove();
        this.triggerAction(user);
      } else {
        this.dropped = false;
      }
    }, 20, GameStory.getPlugin());
  }

  @Override
  protected DropItemAtEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
    return new DropItemAtEvent<>(this.location.clone().setExWorld(chapter.getWorld()),
        this.character != null ? section.getChapter().getCharacter(this.character.getName()) : null,
        this.item != null ? this.item.clone(reader) : null,
        this.material, this.amount);
  }

  @Override
  public Type getType() {
    return Type.DROP_ITEM_AT;
  }
}
