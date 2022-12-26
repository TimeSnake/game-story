/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;

public class AreaEvent<Action extends TriggeredAction> extends LocationEvent<Action> {

    public static final String NAME = "area";

    protected final double radius;

    protected AreaEvent(ExLocation location, StoryCharacter<?> character, double radius) {
        super(location, character);
        this.radius = radius;
    }

    public AreaEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger) throws StoryParseException {
        super(action, bookBuilder, trigger);

        Double radius;
        try {
            radius = trigger.getDouble("radius");
        } catch (ClassCastException e) {
            radius = trigger.getLong("radius").doubleValue();
        }

        if (radius == null) {
            throw new MissingArgumentException("radius");
        }

        this.radius = radius;

    }

    public AreaEvent(Action action, StoryBookBuilder bookBuilder, Toml trigger, double radius) throws StoryParseException {
        super(action, bookBuilder, trigger);
        this.radius = radius;
    }

    @StoryEvent
    public void onUserMove(AsyncUserMoveEvent e) {
        if (this.action.getReader() == null || (!this.action.getReader().containsUser(((StoryUser) e.getUser()))
                && !this.action.getReader().containsUser((StoryUser) e.getUser())) || !this.action.isActive()) {
            return;
        }

        StoryUser user = ((StoryUser) e.getUser());

        if (!user.getLocation().getWorld().equals(this.location.getWorld())) {
            return;
        }

        if (user.getLocation().distance(this.location) <= this.radius) {
            this.triggerAction(user);
        }
    }

    @Override
    protected AreaEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new AreaEvent<>(this.location.clone().setExWorld(chapter.getWorld()), this.character != null ?
                section.getChapter().getCharacter(this.character.getName()) : null, this.radius);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}
