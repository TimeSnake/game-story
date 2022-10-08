/*
 * game-story.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.entities.entity.bukkit.ExArmorStand;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityDestroy;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityEquipment;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityMetadata;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutSpawnEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.EulerAngle;

import java.util.List;

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

    public ItemSearchAction(Toml action, int id, List<Integer> diaryPages)
            throws ItemNotFoundException, CharacterNotFoundException, UnknownLocationException {
        super(action, id, diaryPages);

        this.item = StoryServer.getItem(action.getString(ITEM));
        double itemAngle;
        try {
            itemAngle = action.getDouble(ANGLE);
        } catch (ClassCastException e) {
            itemAngle = action.getLong(ANGLE).doubleValue();
        }
        this.itemAngle = itemAngle;
    }

    @Override
    public ItemSearchAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemSearchAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
                this.item.clone(reader), this.itemAngle);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.collectItem(user);
        this.startNext();
    }

    private void collectItem(StoryUser user) {
        user.addItem(this.item.getItem());
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
                    List.of(new Tuple<>(EquipmentSlot.HAND, this.item.getItem()))));
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
