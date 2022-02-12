package de.timesnake.game.story.action;

import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class StoryAction implements Iterator<StoryAction> {

    public static final String DIARY = "diary";


    protected StoryUser reader;
    protected Set<StoryUser> listeners = new HashSet<>();

    protected final int id;

    protected BaseComponent[] diaryPage;

    protected StorySection section;
    protected StoryAction next;

    protected boolean active;

    protected StoryAction(int id, BaseComponent[] diaryPage) {
        this.id = id;
        this.diaryPage = diaryPage;
    }

    protected StoryAction(int id, BaseComponent[] diaryPage, StoryAction next) {
        this.id = id;
        this.diaryPage = diaryPage;
        this.next = next;
    }

    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners) {
        StoryAction cloned = this.hasNext() ? this.clone(reader, listeners, this.next.clone(reader, listeners)) : this.clone(reader, listeners, null);
        cloned.reader = reader;
        cloned.listeners = listeners;
        return cloned;
    }

    public abstract StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext);

    public StoryAction getNext() {
        return next;
    }

    public void start() {
        this.active = true;
    }

    @Override
    public StoryAction next() {
        return this.next;
    }

    public void setNext(StoryAction next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    public void startNext() {
        this.active = false;

        this.reader.getDiary().addPage(this.diaryPage);
        this.reader.updateDiary();

        for (StoryUser user : this.listeners) {
            user.getDiary().addPage(this.diaryPage);
            user.updateDiary();
        }

        if (this.hasNext()) {
            this.next.start();
        } else {
            this.reader.onCompletedSection(this.section, this.listeners);
        }
    }

    public boolean isActive() {
        return active;
    }

    public StoryUser getReader() {
        return reader;
    }

    public Set<StoryUser> getListeners() {
        return listeners;
    }

    public void setSection(StorySection section) {
        this.section = section;
        if (this.hasNext()) {
            this.next.setSection(section);
        }
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
