package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryUser;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

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
    private StoryPart part;

    private final StoryAction firstAction;

    private final ExLocation startLocation;

    public StorySection(StoryPart part, int id, StoryUser reader, Set<StoryUser> listeners, ExLocation startLocation, StoryAction firstAction) {
        this.part = part;
        this.id = id;
        this.reader = reader;
        this.listeners = listeners;
        this.startLocation = startLocation.clone().setExWorld(reader.getStoryWorld());
        this.firstAction = firstAction.clone(this, reader, listeners);
    }

    public StorySection(ChapterFile file, int partId, int id, StoryAction firstAction) {
        this.id = id;
        this.startLocation = new ExLocation(null, file.getDoubleTriple(ExFile.toPath(ChapterFile.getSectionPath(partId, id), START_LOCATION), X, Y, Z));
        this.firstAction = firstAction;
        this.firstAction.setSection(this);
    }

    public StorySection clone(StoryPart part, StoryUser reader, Set<StoryUser> listeners) {
        return new StorySection(part, this.id, reader, listeners, this.startLocation, this.firstAction);
    }

    public int getId() {
        return id;
    }

    public StoryPart getPart() {
        return part;
    }

    public void setPart(StoryPart part) {
        this.part = part;
    }

    public void start(boolean teleport, boolean spawnEntities) {
        if (teleport) {
            this.reader.teleport(this.startLocation);
            this.listeners.forEach(u -> u.teleport(this.startLocation));

            StorySection previous = this.getPart().getSection(this.id - 1);

            if (previous != null) {
                this.getPart().getDiary().loadPage(previous.firstAction.getDiaryPages().toArray(new Integer[0]));
            }

            this.reader.addPotionEffect(PotionEffectType.BLINDNESS, 20 * 7, 0);
            this.reader.lockLocation(true);

            Server.runTaskTimerSynchrony((t) -> {

                for (int angle = 0; angle < 360; angle += 10) {
                    double x = (Math.sin(angle)) * 0.7;
                    double z = (Math.cos(angle)) * 0.7;

                    Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(102, 0, 102), 1.2f);
                    this.startLocation.getWorld().spawnParticle(Particle.REDSTONE, this.startLocation.getX() + x,
                            this.startLocation.getY(), this.startLocation.getZ() + z, 8, 0, 1.5, 0, 5, dust);
                }

            }, 8, true, 0, 10, GameStory.getPlugin());

            Server.runTaskLaterSynchrony(() -> this.reader.lockLocation(false), 20 * 6, GameStory.getPlugin());
        }

        if (spawnEntities) {
            Server.runTaskLaterSynchrony(() -> {
                Server.printText(Plugin.STORY, "Starting section " + this.id, this.reader.getName());
                int delay = 0;
                for (StoryAction action : this) {
                    Server.runTaskLaterSynchrony(action::spawnEntities, delay, GameStory.getPlugin());
                    delay += 10;
                }

                this.firstAction.start();
            }, 40, GameStory.getPlugin());
        }

    }

    public void stop() {
        Server.runTaskLaterSynchrony(this::clearEntities, 10 * 20, GameStory.getPlugin());
    }

    @Override
    public Iterator<StoryAction> iterator() {
        return new StoryAction.ActionIterator(this.firstAction);
    }

    public void clearEntities() {
        this.forEach(StoryAction::despawnEntities);
    }

    public ExLocation getStartLocation() {
        return this.startLocation;
    }
}
