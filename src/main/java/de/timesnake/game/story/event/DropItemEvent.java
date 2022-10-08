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

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DropItemEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements Listener {

    public static final String NAME = "drop";

    private static final String ITEM = "item";
    private static final String CLEAR_ITEM = "clear";

    protected final StoryItem item;
    protected final boolean clearItem;

    protected DropItemEvent(StoryItem item, boolean clearItem) {
        super();
        this.item = item;
        this.clearItem = clearItem;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public DropItemEvent(Action action, Toml trigger) throws ItemNotFoundException {
        super(action);

        this.item = StoryServer.getItem(trigger.getString(ITEM));

        this.clearItem = trigger.getBoolean(CLEAR_ITEM);
    }

    @Override
    protected DropItemEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new DropItemEvent<>(this.item.clone(reader), this.clearItem);
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM;
    }

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {

        StoryUser user = (StoryUser) e.getUser();

        if (!this.action.getReader().containsUser(user)) {
            return;
        }

        if (!this.action.isActive()) {
            return;
        }

        if (!this.item.getItem().equals(ExItemStack.getItem(e.getItemStack(), false))) {
            return;
        }

        super.triggerAction(user);

        if (this.clearItem) {
            Server.runTaskLaterSynchrony(() -> e.getItemDrop().remove(), 20, GameStory.getPlugin());
        }
    }
}
