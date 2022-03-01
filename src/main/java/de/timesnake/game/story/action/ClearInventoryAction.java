package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public class ClearInventoryAction extends TriggeredAction {

    public static final String NAME = "clear_inventory";

    protected ClearInventoryAction(int id, StoryAction next) {
        super(id, next);
    }

    public ClearInventoryAction(int id, List<Integer> diaryPages) {
        super(id, diaryPages);
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ClearInventoryAction(this.id, clonedNext);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.reader.clearInventory();
        this.listeners.forEach(User::clearInventory);
        this.startNext();
    }
}
