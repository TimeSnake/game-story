package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;
import java.util.Set;

public class ItemGiveAction extends LocationAction {

    public static final String NAME = "item_give";

    private final StoryItem item;

    public ItemGiveAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(id, next, location, character);
        this.item = item;
    }

    public ItemGiveAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException {
        super(id, diaryPages, file, actionPath);

        int itemId = file.getInt(ExFile.toPath(actionPath, ITEM));
        this.item = StoryServer.getItem(itemId);
    }


    @Override
    public ItemGiveAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners,
                                StoryAction clonedNext) {
        return new ItemGiveAction(this.id, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()),
                section.getPart().getCharacter(character.getId()), this.item.clone(reader));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0), this.item.getItem());
        this.startNext();
    }
}
