/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import de.timesnake.game.story.listener.StoryEventListener;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class StoryAction implements Iterator<StoryAction>, StoryEventListener {

    public static final String CHARACTER = "character";
    public static final String MESSAGES = "messages";

    public static final String MESSAGE_PLAYER = "p";
    public static final String MESSAGE_CHARACTER = "c";
    public static final String AUDIO = "a";

    public static final String CHARACTER_LOOK_DIRECTION = "character_look_direction";

    public static final String RADIUS = "radius";

    public static final String LOCATION = "location";

    protected final int id;
    protected Quest quest;
    protected StoryAction next;
    protected boolean active;
    protected List<Integer> diaryPages;

    protected StoryReader reader;

    protected StoryAction(int id, List<Integer> diaryPages) {
        this.id = id;
        this.diaryPages = diaryPages;
    }

    protected StoryAction(int id, StoryAction next) {
        this.id = id;
        this.next = next;
    }

    public StoryAction clone(Quest quest, StoryReader reader, StoryChapter chapter) {
        StoryAction cloned;
        if (this.hasNext()) {
            cloned = this.clone(quest, reader, this.next.clone(quest, reader, chapter), chapter);
        } else {
            cloned = this.clone(quest, reader, null, chapter);
        }
        cloned.reader = reader;
        cloned.diaryPages = this.diaryPages;
        cloned.quest = quest;
        return cloned;
    }

    public abstract StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter);

    public StoryAction getNext() {
        return next;
    }

    public void setNext(StoryAction next) {
        this.next = next;
    }

    public void start() {
        this.active = true;
        StoryServer.getEventManager().registerListeners(this);
    }

    @Override
    public StoryAction next() {
        return this.next;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    public void stop() {
        this.active = false;
        StoryServer.getEventManager().unregisterListeners(this);

        if (this.diaryPages != null) {
            this.quest.getChapter().getDiary().loadPage(this.diaryPages.toArray(new Integer[0]));
        }
    }

    public void startNext() {
        this.stop();
        if (this.hasNext()) {
            this.next.start();
        } else {
            this.reader.onCompletedQuest(this.quest);
        }
    }

    public List<Integer> getDiaryPages() {
        return diaryPages;
    }

    public void spawnEntities() {

    }

    public void despawnEntities() {

    }

    public boolean isActive() {
        return active;
    }

    public StoryReader getReader() {
        return reader;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
        if (this.hasNext()) {
            this.next.setQuest(quest);
        }
    }

    public Collection<String> getCharacterNames() {
        return new HashSet<>();
    }

    public static class ActionIterator implements Iterator<StoryAction> {

        private StoryAction next;

        public ActionIterator(StoryAction action) {
            this.next = action;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public StoryAction next() {
            StoryAction current = this.next;
            this.next = current.getNext();
            return current;
        }
    }
}
