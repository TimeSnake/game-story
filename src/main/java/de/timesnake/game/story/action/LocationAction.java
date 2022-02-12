package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.structure.ChapterFile;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class LocationAction extends TriggeredAction {

    private static final String LOCATION = "location";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    private static final String YAW = "yaw";
    private static final String PITCH = "pitch";


    protected ExLocation location;

    protected LocationAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location) {
        super(id, diaryPage, next);
        this.location = location;
    }

    public LocationAction(int id, BaseComponent[] diaryPage, boolean yawPitch, ChapterFile file, String actionPath) {
        super(id, diaryPage);
        this.location = new ExLocation(null, file.getActionValueDoubleTriple(actionPath, LOCATION, X, Y, Z));

        if (yawPitch) {
            this.location.setYaw(file.getActionValueDouble(actionPath, LOCATION + "." + YAW).floatValue());
            this.location.setPitch(file.getActionValueDouble(actionPath, LOCATION + "." + PITCH).floatValue());
        }
    }
}
