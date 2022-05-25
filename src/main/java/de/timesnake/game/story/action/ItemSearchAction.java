package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.entities.entity.bukkit.ExArmorStand;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityDestroy;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityEquipment;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityMetadata;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutSpawnEntityLiving;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.EulerAngle;

import java.util.List;
import java.util.Set;

public class ItemSearchAction extends LocationAction {

    public static final String NAME = "item_search";

    private static final String ITEM = "item";
    private static final String ANGLE = "angle";

    private final StoryItem item;
    private final double itemAngle;

    private ExArmorStand entity;

    public ItemSearchAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character,
                            StoryItem item, double itemAngle) {
        super(id, next, location, character);
        this.item = item;
        this.itemAngle = itemAngle;
    }

    public ItemSearchAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) throws ItemNotFoundException, CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPages, file, actionPath);

        int itemId = file.getInt(ExFile.toPath(actionPath, ITEM));
        this.item = StoryServer.getItem(itemId);
        this.itemAngle = file.getDouble(ExFile.toPath(actionPath, ANGLE));
    }

    @Override
    public ItemSearchAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners,
                                  StoryAction clonedNext) {
        return new ItemSearchAction(this.id, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()),
                this.character != null ? section.getPart().getCharacter(this.character.getId()) : null,
                this.item.clone(reader), this.itemAngle);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (!user.equals(this.reader)) {
            return;
        }

        this.collectItem();
        this.startNext();
    }

    private void collectItem() {
        this.reader.addItem(this.item.getItem());
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

        this.reader.sendPacket(ExPacketPlayOutSpawnEntityLiving.wrap(this.entity));
        this.reader.sendPacket(ExPacketPlayOutEntityMetadata.wrap(this.entity,
                ExPacketPlayOutEntityMetadata.DataType.UPDATE));
        this.reader.sendPacket(ExPacketPlayOutEntityEquipment.wrap(this.entity,
                List.of(new Tuple<>(EquipmentSlot.HAND, this.item.getItem()))));

        for (StoryUser listener : this.listeners) {
            listener.sendPacket(ExPacketPlayOutSpawnEntityLiving.wrap(this.entity));
            listener.sendPacket(ExPacketPlayOutEntityMetadata.wrap(this.entity,
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE));
            listener.sendPacket(ExPacketPlayOutEntityEquipment.wrap(this.entity,
                    List.of(new Tuple<>(EquipmentSlot.HAND, this.item.getItem()))));
        }
    }

    @Override
    public void despawnEntities() {
        if (this.entity != null) {
            this.reader.sendPacket(ExPacketPlayOutEntityDestroy.wrap(this.entity));

            for (StoryUser listener : this.listeners) {
                listener.sendPacket(ExPacketPlayOutEntityDestroy.wrap(this.entity));
            }
        }
    }
}
