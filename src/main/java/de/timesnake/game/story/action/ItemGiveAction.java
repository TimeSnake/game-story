package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.StoryItem;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class ItemGiveAction extends RadiusAction {

    public static final String NAME = "item_give";

    private static final String CHARACTER = "character";
    private static final String ITEM = "item";

    private final StoryCharacter<?> character;
    private final StoryItem item;

    public ItemGiveAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, Double radius, StoryCharacter<?> character, StoryItem item) {
        super(id, diaryPage, next, location, radius);
        this.character = character;
        this.item = item;
    }

    public ItemGiveAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, ItemNotFoundException {
        super(id, diaryPage, false, file, actionPath);

        int charId = file.getActionValueInteger(actionPath, CHARACTER);
        this.character = StoryServer.getCharater(charId);

        this.location = character.getLocation();

        int itemId = file.getActionValueInteger(actionPath, ITEM);
        this.item = StoryServer.getItem(itemId);
    }

    @Override
    protected void onUserNearby(StoryUser user) {
        this.location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0), this.item.getItem());
        this.startNext();
    }

    @Override
    public ItemGiveAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new ItemGiveAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), this.radius, this.character.clone(reader, listeners), this.item.clone(reader));
    }
}
