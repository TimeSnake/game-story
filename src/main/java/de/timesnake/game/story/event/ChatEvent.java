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
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.MissingArgumentException;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;

import java.util.List;

public class ChatEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements UserChatCommandListener {

    public static final String NAME = "chat_code";

    private static final String CODE = "code";

    private final List<String> codes;
    private StoryReader reader;

    protected ChatEvent(StoryReader reader, List<String> codes) {
        super();
        this.codes = codes;
        this.reader = reader;

        this.reader.forEach(u -> Server.getUserEventManager().addUserChatCommand(u, this));
    }

    public ChatEvent(Action action, Toml trigger) throws MissingArgumentException {
        super(action);
        if (trigger.containsPrimitive(CODE)) {
            this.codes = List.of(trigger.getString(CODE));
        } else {
            this.codes = trigger.getList(CODE);
        }

        if (this.codes == null) {
            throw new MissingArgumentException("code");
        }
    }

    @Override
    protected TriggerEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new ChatEvent<>(reader, this.codes);
    }

    @Override
    public Type getType() {
        return Type.CHAT_CODE;
    }

    @Override
    public void onUserChatCommand(UserChatCommandEvent event) {
        if (!this.action.isActive()) {
            event.removeLisener(false);
            event.setCancelled(false);
            return;
        }

        Server.printText(Plugin.STORY, Server.getChatManager().getSenderMember(event.getUser()) + event.getMessage());

        if (!this.codes.contains(event.getMessage())) {
            event.removeLisener(false);
            event.setCancelled(true);
            event.getUser().sendMessage(Server.getChat().getSenderMember(event.getUser()) + "§7" + event.getMessage());
            event.getUser().sendPluginMessage(Plugin.STORY, Component.text("Leider falsch", ExTextColor.WARNING));
            return;
        }

        event.getUser().sendPluginMessage(Plugin.STORY, Component.text("Richtig", ExTextColor.GREEN));
        this.triggerAction((StoryUser) event.getUser());

        event.removeLisener(true);
        event.setCancelled(true);

        this.reader.forEach(u -> Server.getUserEventManager().removeUserChatCommand(u, this));
    }
}
