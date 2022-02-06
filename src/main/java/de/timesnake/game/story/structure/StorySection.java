package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.EntityAction;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryUser;

import java.util.Iterator;
import java.util.Set;

public class StorySection implements Iterable<StoryAction> {

    private static final String START_LOCATION = "start.location";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    private StoryUser reader;
    private Set<StoryUser> listeners;

    private final int id;
    private final StoryAction firstAction;

    private final ExLocation startLocation;

    public StorySection(int id, StoryUser reader, Set<StoryUser> listeners, ExLocation startLocation, StoryAction firstAction) {
        this.id = id;
        this.reader = reader;
        this.listeners = listeners;
        this.startLocation = startLocation;
        this.firstAction = firstAction;
        this.firstAction.setSection(this);
    }

    public StorySection(ChapterFile file, int partId, int id, StoryAction firstAction) {
        this.id = id;
        this.startLocation = new ExLocation(null, file.getSectionValueDoubleTriple(ChapterFile.getSectionPath(partId, id), START_LOCATION, X, Y, Z));
        this.firstAction = firstAction;
        this.firstAction.setSection(this);
    }

    public StorySection clone(StoryUser reader, Set<StoryUser> listeners) {
        return new StorySection(this.id, reader, listeners, this.startLocation.clone().setExWorld(reader.getStoryWorld()), this.firstAction.clone(reader, listeners));
    }

    public int getId() {
        return id;
    }

    public void start() {
        this.reader.teleport(this.startLocation);

        for (StoryUser listener : this.listeners) {
            listener.teleport(this.startLocation);
        }

        Server.runTaskLaterSynchrony(() -> {
            Server.printText(Plugin.STORY, "Starting section " + this.id);
            for (StoryAction action : this) {
                if (action instanceof EntityAction) {
                    ((EntityAction) action).spawnEntities();
                }
            }

            this.firstAction.start();
        }, 20, GameStory.getPlugin());
    }

    public void stop() {
        Server.runTaskLaterSynchrony(this::clearEntities, 200, GameStory.getPlugin());
    }

    @Override
    public Iterator<StoryAction> iterator() {
        return new StoryAction.ActionIterator(this.firstAction);
    }

    public void clearEntities() {
        for (StoryAction action : this) {
            if (action instanceof EntityAction) {
                ((EntityAction) action).despawnEntities();
            }
        }
    }
}
