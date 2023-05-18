/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.StoryItem;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemGiveAction extends LocationAction {

  public static final String NAME = "item_give";
  private final Supplier<Integer> amount;
  private StoryItem item;
  private Material material;

  public ItemGiveAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
      StoryItem item,
      Material material, Supplier<Integer> amount) {
    super(id, next, location, character);
    this.item = item;
    this.material = material;
    this.amount = amount;
  }

  public ItemGiveAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id,
      List<Integer> diaryPages) throws StoryParseException {
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

    Supplier<Integer> amount;
    try {
      amount = quest.parseAdvancedInt(action, "amount");
    } catch (MissingArgumentException e) {
      amount = () -> 1;
    }
    this.amount = amount;
  }

  @Override
  public ItemGiveAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
      StoryChapter chapter) {
    return new ItemGiveAction(this.id, clonedNext,
        this.location.clone().setExWorld(chapter.getWorld()),
        quest.getChapter().getCharacter(character.getName()),
        this.item != null ? this.item.clone(reader) : null,
        this.material, this.amount);
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    Server.runTaskSynchrony(() -> {
      if (this.item != null) {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0),
            this.item.getItem().asQuantity(this.amount.get()));
      } else {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0),
            new ItemStack(this.material, this.amount.get()));
      }
    }, GameStory.getPlugin());
    this.startNext();
  }
}
