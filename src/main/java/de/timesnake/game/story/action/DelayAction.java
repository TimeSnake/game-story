/*
 * timesnake.game-story.main
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
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.List;

public class DelayAction extends StoryAction {

    public static final String NAME = "delay";

    private final int delay;

    protected DelayAction(int id, StoryAction next, int delay) {
        super(id, next);
        this.delay = delay;
    }

    public DelayAction(Toml action, int id, List<Integer> diaryPages) throws MissingArgumentException {
        super(id, diaryPages);

        Long delay = action.getLong("delay", 0L);

        if (delay == null) {
            throw new MissingArgumentException("delay");
        }

        this.delay = delay.intValue();
    }

    @Override
    public void start() {
        super.start();
        Server.runTaskLaterSynchrony(this::startNext, 20 * this.delay, GameStory.getPlugin());
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new DelayAction(this.id, clonedNext, this.delay);
    }
}
