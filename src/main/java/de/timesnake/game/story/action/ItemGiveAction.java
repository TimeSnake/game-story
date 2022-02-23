package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class ItemGiveAction extends LocationAction {

    public static final String NAME = "item_give";

    private final StoryItem item;

    public ItemGiveAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(id, diaryPage, next, location, character);
        this.item = item;
    }

    public ItemGiveAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException {
        super(id, diaryPage, file, actionPath);

        int itemId = file.getInt(ExFile.toPath(actionPath, ITEM));
        this.item = StoryServer.getItem(itemId);
    }


    @Override
    public ItemGiveAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ItemGiveAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), this.character.clone(reader, listeners), this.item.clone(reader));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0), this.item.getItem());
        this.startNext();
    }
}
