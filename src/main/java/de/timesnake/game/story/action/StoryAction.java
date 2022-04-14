package de.timesnake.game.story.action;

import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;

import java.util.*;

public abstract class StoryAction implements Iterator<StoryAction> {

    public static final String DIARY = "diary";

    public static final String CHARACTER = "character";
    public static final String MESSAGES = "messages";

    public static final String MESSAGE_PLAYER = "p";
    public static final String MESSAGE_CHARACTER = "c";

    public static final String CHARACTER_LOOK_DIRECTION = "character_look_direction";
    public static final String YAW = "yaw";
    public static final String PITCH = "pitch";

    public static final String RADIUS = "radius";

    public static final String LOCATION = "location";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";

    public static final String DELAY = "delay";

    public static final String ITEM = "item";


    protected StoryUser reader;
    protected Set<StoryUser> listeners = new HashSet<>();

    protected final int id;

    protected StorySection section;
    protected StoryAction next;

    private List<Integer> diaryPages;

    protected boolean active;

    protected StoryAction(int id, List<Integer> diaryPages) {
        this.id = id;
        this.diaryPages = diaryPages;
    }

    protected StoryAction(int id, StoryAction next) {
        this.id = id;
        this.next = next;
    }

    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners) {
        StoryAction cloned = this.hasNext() ? this.clone(section, reader, listeners, this.next.clone(section, reader, listeners)) : this.clone(section, reader, listeners, null);
        cloned.reader = reader;
        cloned.listeners = listeners;
        cloned.diaryPages = this.diaryPages;
        cloned.section = section;
        return cloned;
    }

    public abstract StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext);

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

        this.section.getPart().getDiary().loadPage(this.diaryPages.toArray(new Integer[0]));

        if (this.hasNext()) {
            this.next.start();
        } else {
            this.reader.onCompletedSection(this.section, this.listeners);
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

    public Collection<Integer> getCharacterIds() {
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
