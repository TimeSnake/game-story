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
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.block.data.*;

import java.util.List;

public class BlockInteractAction extends LocationAction {

    public static final String NAME = "block_interact";

    protected BlockInteractAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character) {
        super(id, next, location, character);
    }

    public BlockInteractAction(Toml action, int id, List<Integer> diaryPages) throws CharacterNotFoundException,
            UnknownLocationException {
        super(action, id, diaryPages);
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new BlockInteractAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? this.character.clone(reader, chapter) : null);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.interact();
        this.startNext();
    }

    private void interact() {
        BlockData blockData = this.location.getBlock().getBlockData();
        if (blockData instanceof Openable openable) {
            openable.setOpen(!openable.isOpen());
        } else if (blockData instanceof Lightable lightable) {
            lightable.setLit(!lightable.isLit());
        } else if (blockData instanceof AnaloguePowerable powerable) {
            powerable.setPower(powerable.getPower() == 0 ? 15 : 0);
        } else if (blockData instanceof Powerable powerable) {
            powerable.setPowered(!powerable.isPowered());
        }
        this.location.getBlock().setBlockData(blockData);
    }
}
