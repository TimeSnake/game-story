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

import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.listener.StoryEventListener;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

public abstract class TriggerEvent<Action extends TriggeredAction> implements StoryEventListener {

    protected Action action;

    protected TriggerEvent() {

    }

    public TriggerEvent(Action action) {
        this.action = action;
    }

    public void triggerAction(StoryUser user) {
        this.action.trigger(this.getType(), user);
    }

    public TriggerEvent<Action> clone(Quest section, StoryReader reader, Action clonedAction, StoryChapter chapter) {
        TriggerEvent<Action> cloned = this.clone(section, reader, chapter);
        cloned.action = clonedAction;
        return cloned;
    }

    public void start() {
        StoryServer.getEventManager().registerListeners(this);
    }

    public void stop() {
        StoryServer.getEventManager().unregisterListeners(this);
    }

    protected abstract TriggerEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter);

    public abstract Type getType();

    public enum Type {
        AREA,
        SNEAK,
        DROP_ITEM,
        DROP_ITEM_AT,
        START,
        SLEEP,
        CHAT_CODE,
        DELAY
    }
}
