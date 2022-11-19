/*
 * workspace.game-story.main
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

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.function.Supplier;

public class DelayEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    public static final String NAME = "delay";

    private final Supplier<Integer> delay;

    protected DelayEvent(StoryReader reader, Supplier<Integer> delay) {
        super();
        this.delay = delay;
    }

    public DelayEvent(Quest quest, Action action, Toml trigger) throws StoryParseException {
        super(action);
        this.delay = quest.parseAdvancedInt(trigger, "delay");
    }

    @Override
    protected DelayEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new DelayEvent<>(reader, this.delay);
    }

    @Override
    public void start() {
        super.start();
        Server.runTaskLaterSynchrony(() -> this.triggerAction(null), 20 * this.delay.get(), GameStory.getPlugin());
    }

    @Override
    public Type getType() {
        return Type.DELAY;
    }
}
