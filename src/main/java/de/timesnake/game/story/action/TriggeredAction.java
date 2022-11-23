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

import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;

public abstract class TriggeredAction extends StoryAction {

    protected TriggerEvent<TriggeredAction> triggerEvent;

    protected TriggeredAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    protected TriggeredAction(int id, StoryAction next) {
        super(id, next);
    }

    public void setTriggerEvent(TriggerEvent<TriggeredAction> triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    @Override
    public TriggeredAction clone(Quest quest, StoryReader reader, StoryChapter chapter) {
        TriggeredAction cloned = (TriggeredAction) super.clone(quest, reader, chapter);
        if (this.triggerEvent != null) {
            cloned.triggerEvent = this.triggerEvent.clone(quest, reader, cloned, chapter);
        }
        return cloned;
    }

    @Override
    public void start() {
        super.start();

        if (this.triggerEvent == null) {
            this.trigger(TriggerEvent.Type.START, this.reader.anyUser());
        } else {
            this.triggerEvent.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        if (this.triggerEvent != null) {
            this.triggerEvent.stop();
        }
    }

    public abstract void trigger(TriggerEvent.Type type, StoryUser user);

}
