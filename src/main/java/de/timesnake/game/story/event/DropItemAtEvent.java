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
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DropItemAtEvent<Action extends TriggeredAction> extends LocationEvent<Action> implements Listener {

    public static final String NAME = "drop_at";

    private static final String ITEM = "item";


    private final StoryItem item;

    private boolean dropped = false;

    protected DropItemAtEvent(ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(location, character);

        this.item = item;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public DropItemAtEvent(Action action, Toml trigger) throws ItemNotFoundException,
            CharacterNotFoundException, UnknownLocationException {
        super(action, trigger);

        this.item = StoryServer.getItem(trigger.getString(ITEM));
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

        if (!this.location.getExWorld().equals(user.getExWorld())) {
            return;
        }

        if (this.dropped) {
            return;
        }

        this.dropped = true;

        Server.runTaskLaterSynchrony(() -> {
            if (this.location.distanceSquared(e.getItemDrop().getLocation()) < 2) {
                e.getItemDrop().remove();
                this.triggerAction(user);
            } else {
                this.dropped = false;
            }
        }, 20, GameStory.getPlugin());
    }

    @Override
    protected DropItemAtEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new DropItemAtEvent<>(this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? section.getChapter().getCharacter(this.character.getName()) : null,
                this.item.clone(reader));
    }

    @Override
    public Type getType() {
        return Type.DROP_ITEM_AT;
    }
}
