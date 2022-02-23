package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class ClearInventoryAction extends TriggeredAction {

    public static final String NAME = "clear_inventory";

    protected ClearInventoryAction(int id, BaseComponent[] diaryPage, StoryAction next) {
        super(id, diaryPage, next);
    }

    public ClearInventoryAction(int id, BaseComponent[] diaryPage) {
        super(id, diaryPage);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ClearInventoryAction(this.id, this.diaryPage, clonedNext);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.reader.clearInventory();
        this.listeners.forEach(User::clearInventory);
        this.startNext();
    }
}
