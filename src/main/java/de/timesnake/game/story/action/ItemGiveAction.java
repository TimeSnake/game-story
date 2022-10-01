package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.*;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

import java.util.List;

public class ItemGiveAction extends LocationAction {

    public static final String NAME = "item_give";

    private final StoryItem item;

    public ItemGiveAction(int id, StoryAction next, ExLocation location, StoryCharacter<?> character, StoryItem item) {
        super(id, next, location, character);
        this.item = item;
    }

    public ItemGiveAction(Toml action, int id, List<Integer> diaryPages)
            throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException {
        super(action, id, diaryPages);

        this.item = StoryServer.getItem(action.getString("item"));
    }


    @Override
    public ItemGiveAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new ItemGiveAction(this.id, clonedNext, this.location.clone().setExWorld(chapter.getWorld()),
                quest.getChapter().getCharacter(character.getName()), this.item.clone(reader));
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0), this.item.getItem());
        this.startNext();
    }
}
