/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.chat.ExTextColor;
import java.util.List;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;

public class ChatEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements
        UserChatCommandListener {

    public static final String NAME = "chat_code";

    private static final String CODE = "code";

    private final List<Supplier<String>> codes;
    private StoryReader reader;

    protected ChatEvent(StoryReader reader, List<Supplier<String>> codes) {
        super();
        this.codes = codes;
        this.reader = reader;

        this.reader.forEach(u -> Server.getUserEventManager().addUserChatCommand(u, this));
    }

    public ChatEvent(Quest quest, Action action, Toml trigger) throws MissingArgumentException {
        super(action);
        if (trigger.containsPrimitive(CODE)) {
            this.codes = List.of(quest.parseString(trigger.getString(CODE)));
        } else {
            this.codes = trigger.getList(CODE).stream().map(m -> quest.parseString(((String) m)))
                    .toList();
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

        Loggers.GAME.info(
                Server.getChatManager().getSenderMember(event.getUser()) + event.getMessage());

        if (this.codes.stream().anyMatch(c -> c.get().equals(event.getMessage()))) {
            event.removeLisener(false);
            event.setCancelled(true);
            event.getUser().sendMessage(
                    Server.getChat().getSenderMember(event.getUser()) + "§7" + event.getMessage());
            event.getUser().sendPluginMessage(Plugin.STORY,
                    Component.text("Leider falsch", ExTextColor.WARNING));
            return;
        }

        event.getUser()
                .sendPluginMessage(Plugin.STORY, Component.text("Richtig", ExTextColor.GREEN));
        this.triggerAction((StoryUser) event.getUser());

        event.removeLisener(true);
        event.setCancelled(true);

        this.reader.forEach(u -> Server.getUserEventManager().removeUserChatCommand(u, this));
    }
}
