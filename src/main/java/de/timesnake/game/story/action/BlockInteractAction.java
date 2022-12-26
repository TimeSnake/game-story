/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
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

    public BlockInteractAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id, List<Integer> diaryPages)
            throws StoryParseException {
        super(bookBuilder, action, id, diaryPages);
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
        Server.runTaskSynchrony(() -> {
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
        }, GameStory.getPlugin());

    }
}
