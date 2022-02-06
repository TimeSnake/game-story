package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class LocationSearchAction extends RadiusAction {

    public static final String NAME = "location_search";

    public LocationSearchAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, Double radius) {
        super(id, diaryPage, next, location, radius);
    }

    public LocationSearchAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) {
        super(id, diaryPage, false, file, actionPath);
    }

    @Override
    protected void onUserNearby(StoryUser user) {
        if (!this.reader.equals(user)) {
            return;
        }

        this.startNext();
    }

    @Override
    public LocationSearchAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new LocationSearchAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), radius);
    }
}
