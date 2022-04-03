package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.Set;

public class ChatEvent<Action extends TriggeredAction> extends TriggerEvent<Action> implements UserChatCommandListener {

    public static final String NAME = "chat_code";

    private static final String CODE = "code";

    private final String code;

    protected ChatEvent(StoryUser reader, String code) {
        super();
        this.code = code;

        Server.getUserEventManager().addUserChatCommand(reader, this);
    }

    public ChatEvent(Action action, ChapterFile file, String triggerPath) {
        super(action);
        this.code = file.getString(ExFile.toPath(triggerPath, CODE));
    }

    @Override
    protected TriggerEvent<Action> clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        return new ChatEvent<>(reader, this.code);
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

        if (!this.code.equals(event.getMessage())) {
            event.removeLisener(false);
            event.setCancelled(true);
            event.getUser().sendPluginMessage(Plugin.STORY, "§cLeider falsch");
            return;
        }

        this.triggerAction((StoryUser) event.getUser());

        event.removeLisener(true);
        event.setCancelled(true);
    }
}
