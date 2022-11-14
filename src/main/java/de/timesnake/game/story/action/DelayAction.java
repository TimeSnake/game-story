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

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.List;
import java.util.function.Supplier;

public class DelayAction extends StoryAction {

    public static final String NAME = "delay";

    private final Supplier<Integer> delay;

    protected DelayAction(int id, StoryAction next, Supplier<Integer> delay) {
        super(id, next);
        this.delay = delay;
    }

    public DelayAction(Quest quest, Toml action, int id, List<Integer> diaryPages) throws MissingArgumentException,
            InvalidArgumentTypeException {
        super(id, diaryPages);
        this.delay = quest.parseAdvancedInt(action, "delay");
    }

    @Override
    public void start() {
        super.start();
        Server.runTaskLaterSynchrony(this::startNext, 20 * this.delay.get(), GameStory.getPlugin());
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new DelayAction(this.id, clonedNext, this.delay);
    }
}
