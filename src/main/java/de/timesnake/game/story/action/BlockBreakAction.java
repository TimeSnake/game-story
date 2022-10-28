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
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.CancelPriority;
import de.timesnake.basic.bukkit.util.user.event.UserBlockBreakEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.InvalidArgumentTypeException;
import de.timesnake.game.story.elements.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class BlockBreakAction extends StoryAction implements Listener {

    public static final String NAME = "block_break";

    private final List<Material> materials;
    private final BreakArea breakArea;

    protected BlockBreakAction(int id, StoryAction next, BreakArea breakArea, List<Material> materials) {
        super(id, next);
        this.breakArea = breakArea;
        this.materials = materials;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public BlockBreakAction(Toml action, int id, List<Integer> diaryPages)
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
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new BlockBreakAction(this.id, clonedNext, breakArea, materials);
    }

    @Override
    public void start() {
        super.start();
        super.startNext();
    }

    @EventHandler
    public void onUserBreakBlock(UserBlockBreakEvent e) {
        if (!this.isActive() && !this.getNext().isActive()) {
            return;
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

        System.out.println("break");

        e.setCancelled(CancelPriority.HIGH, false);
        e.setDropItems(true);
    }

    private interface BreakArea {

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
