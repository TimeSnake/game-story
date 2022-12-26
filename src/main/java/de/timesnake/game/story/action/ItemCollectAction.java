/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
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
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.entities.entity.bukkit.ExArmorStand;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityDestroy;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityEquipment;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityMetadata;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutSpawnEntity;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.List;

public class ItemCollectAction extends LocationAction {

    public static final String NAME = "item_collect";

    private static final String ANGLE = "angle";

    private final double itemAngle;
    private StoryItem item;
    private Material material;
    private ExArmorStand entity;

    public ItemCollectAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                             StoryItem item, Material material, double itemAngle) {
        super(id, next, location, character);
        this.item = item;
        this.itemAngle = itemAngle;
        this.material = material;
    }

    public ItemCollectAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id, List<Integer> diaryPages)
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

        Double itemAngle;
        try {
            itemAngle = action.getDouble(ANGLE);
        } catch (ClassCastException e) {
            itemAngle = action.getLong(ANGLE).doubleValue();
        }

        if (itemAngle == null) {
            throw new MissingArgumentException("angle");
        }

        this.itemAngle = itemAngle;
    }

    @Override
    public ItemCollectAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemCollectAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
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
        this.entity = new ExArmorStand(this.location.getWorld());

        this.entity.setInvulnerable(true);
        this.entity.setInvisible(true);
        this.entity.setNoGravity(true);
        this.entity.setPosition(this.location.getX() + 0.3, this.location.getY() - 0.8, this.location.getZ());
        this.entity.setRightArmPose(new EulerAngle(this.itemAngle, 0, 0));

        for (StoryUser listener : this.reader) {
            listener.sendPacket(ExPacketPlayOutSpawnEntity.wrap(this.entity));
            listener.sendPacket(ExPacketPlayOutEntityMetadata.wrap(this.entity,
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE));
            listener.sendPacket(ExPacketPlayOutEntityEquipment.wrap(this.entity,
                    List.of(new Tuple<>(EquipmentSlot.HAND,
                            this.item != null ? this.item.getItem() : new ItemStack(this.material)))));
        }
    }

    @Override
    public void despawnEntities() {
        if (this.entity != null) {
            for (StoryUser listener : this.reader) {
                listener.sendPacket(ExPacketPlayOutEntityDestroy.wrap(this.entity));
            }
        }
    }
}
