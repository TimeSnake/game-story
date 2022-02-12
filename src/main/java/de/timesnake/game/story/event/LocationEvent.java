package de.timesnake.game.story.event;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.TriggeredAction;
import de.timesnake.game.story.structure.ChapterFile;

public abstract class LocationEvent<Action extends TriggeredAction> extends TriggerEvent<Action> {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    private static final String YAW = "yaw";
    private static final String PITCH = "pitch";


    protected ExLocation location;

    protected LocationEvent(ExLocation location) {
        super();
        this.location = location;
    }

    public LocationEvent(Action action, ChapterFile file, String triggerPath) {
        super(action);

        this.location = new ExLocation(null, file.getTriggerValueDoubleTriple(triggerPath, "", X, Y, Z));
    }
}
