package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.library.basic.util.Triple;

import java.util.List;
import java.util.Set;

public class ChapterFile extends ExFile {

    private static final String NAME = "name";

    private static final String PARTS = "parts";
    private static final String PART_NAME = "name";
    private static final String PART_END_MESSAGE = "end_message";

    private static final String SECTIONS = "sections";

    private static final String ACTIONS = "actions";

    private static final String TRIGGER = "trigger";
    private static final String TRIGGER_TYPE = "type";

    private static final String ACTION = "action";
    private static final String ACTION_TYPE = "type";

    public ChapterFile(String chapter) {
        super("game-story", "chapter_" + chapter);
    }

    public String getName() {
        return super.getString(NAME);
    }

    public Set<Integer> getPartIds() {
        return super.getPathIntegerList(PARTS);
    }

    public String getPartName(int id) {
        return super.getString(getPartPath(id) + "." + PART_NAME);
    }

    public String getPartEndMessage(int id) {
        return super.getString(getPartPath(id) + "." + PART_END_MESSAGE);
    }

    public Set<Integer> getSectionIdsFromPart(int partId) {
        return super.getPathIntegerList(getPartPath(partId) + "." + SECTIONS);
    }

    public Triple<Double, Double, Double> getSectionValueDoubleTriple(String sectionPath, String subPath, String a, String b, String c) {
        return super.getDoubleTriple(sectionPath + "." + subPath, a, b, c);
    }

    public Set<Integer> getActionIdsFromSection(int partId, int sectionId) {
        return super.getPathIntegerList(getSectionPath(partId, sectionId) + "." + ACTIONS);
    }

    public String getActionType(int partId, int sectionId, int actionId) {
        return super.getString(getActionPath(partId, sectionId, actionId) + "." + ACTION_TYPE);
    }

    public static String getActionPath(int partId, int sectionId, int actionId) {
        return getSectionPath(partId, sectionId) + "." + ACTIONS + "." + actionId + "." + ACTION;
    }

    public String getActionValueString(String actionPath, String subPath) {
        return super.getString(actionPath + "." + subPath);
    }

    public static String getTriggerPath(int partId, int sectionId, int actionId) {
        return getSectionPath(partId, sectionId) + "." + ACTIONS + "." + actionId + "." + TRIGGER;
    }

    public Integer getActionValueInteger(String actionPath, String subPath) {
        return super.getInt(actionPath + "." + subPath);
    }

    public String getTriggerType(int partId, int sectionId, int actionId) {
        return super.getString(getTriggerPath(partId, sectionId, actionId) + "." + TRIGGER_TYPE);
    }

    public Double getActionValueDouble(String actionPath, String subPath) {
        return super.getDouble(actionPath + "." + subPath);
    }

    public String getTriggerValueString(String triggerPath, String subPath) {
        return super.getString(triggerPath + "." + subPath);
    }

    public Integer getTriggerValueInteger(String triggerPath, String subPath) {
        return super.getInt(triggerPath + "." + subPath);
    }

    public ExLocation getActionValueExLocation(String actionPath, String subPath) throws WorldNotExistException {
        return super.getExLocation(actionPath + "." + subPath);
    }

    public List<String> getActionValueStringList(String actionPath, String subPath) {
        return super.getStringList(actionPath + "." + subPath);
    }

    public Triple<Double, Double, Double> getActionValueDoubleTriple(String actionPath, String subPath, String a, String b, String c) {
        return super.getDoubleTriple(actionPath + "." + subPath, a, b, c);
    }

    public Double getTriggerValueDouble(String triggerPath, String subPath) {
        return super.getDouble(triggerPath + "." + subPath);
    }

    public static String getPartPath(int id) {
        return PARTS + "." + id;
    }

    public static String getSectionPath(int partId, int sectionId) {
        return getPartPath(partId) + "." + SECTIONS + "." + sectionId;
    }

    public Boolean getTriggerValueBoolean(String triggerPath, String subPath) {
        return super.getBoolean(triggerPath + "." + subPath);
    }

    public Triple<Double, Double, Double> getTriggerValueDoubleTriple(String triggerPath, String subPath, String a, String b, String c) {
        return super.getDoubleTriple(triggerPath + "." + subPath, a, b, c);
    }
}
