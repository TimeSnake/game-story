/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.event;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.function.Supplier;

public class DelayEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    public static final String NAME = "delay";

    private final Supplier<Integer> delay;

    protected DelayEvent(StoryReader reader, Supplier<Integer> delay) {
        super();
        this.delay = delay;
    }

    public DelayEvent(Quest quest, Action action, Toml trigger) throws StoryParseException {
        super(action);
        this.delay = quest.parseAdvancedInt(trigger, "delay");
    }

    @Override
    protected DelayEvent<Action> clone(Quest section, StoryReader reader, StoryChapter chapter) {
        return new DelayEvent<>(reader, this.delay);
    }

    @Override
    public void start() {
        super.start();
        Server.runTaskLaterSynchrony(() -> this.triggerAction(null), 20 * this.delay.get(), GameStory.getPlugin());
    }

    @Override
    public Type getType() {
        return Type.DELAY;
    }
}
