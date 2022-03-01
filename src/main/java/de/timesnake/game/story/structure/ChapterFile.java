package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.file.ExFile;

import java.util.List;
import java.util.Set;

public class ChapterFile extends ExFile {

    private static final String NAME = "name";

    private static final String PARTS = "parts";
    private static final String PART_NAME = "name";
    private static final String PART_END_MESSAGE = "end_message";

    private static final String SECTIONS = "sections";
    private static final String ACTIONS = "actions";

    private static final String TRIGGER_TYPE = "trigger";
    private static final String ACTION_TYPE = "action";

    private static final String DIARY = "diary";

    private static final String DIARY_TEXT = "text";
    private static final String DIARY_DATE = "date";

    public ChapterFile(String chapter) {
        super("game-story", "chapter_" + chapter);
    }

    public static String getActionPath(int partId, int sectionId, int actionId) {
        return ExFile.toPath(getSectionPath(partId, sectionId), ACTIONS, String.valueOf(actionId));
    }

    public Set<Integer> getPartIds() {
        return super.getPathIntegerList(PARTS);
    }

    public static String getTriggerPath(int partId, int sectionId, int actionId) {
        return ExFile.toPath(getSectionPath(partId, sectionId), ACTIONS, String.valueOf(actionId));
    }

    public static String getPartPath(int id) {
        return ExFile.toPath(PARTS, String.valueOf(id));
    }

    public static String getSectionPath(int partId, int sectionId) {
        return ExFile.toPath(getPartPath(partId), SECTIONS, String.valueOf(sectionId));
    }

    public String getChapterName() {
        return super.getString(NAME);
    }

    public String getPartName(int id) {
        return super.getString(ExFile.toPath(getPartPath(id), PART_NAME));
    }

    public String getPartEndMessage(int id) {
        return super.getString(ExFile.toPath(getPartPath(id), PART_END_MESSAGE));
    }

    public Set<Integer> getSectionIdsFromPart(int partId) {
        return super.getPathIntegerList(ExFile.toPath(getPartPath(partId), SECTIONS));
    }

    public Set<Integer> getActionIdsFromSection(int partId, int sectionId) {
        return super.getPathIntegerList(ExFile.toPath(getSectionPath(partId, sectionId), ACTIONS));
    }

    public String getDiaryPath(int part) {
        return ExFile.toPath(PARTS, String.valueOf(part), DIARY);
    }

    public String getDiaryPagePath(int part, int page) {
        return ExFile.toPath(PARTS, String.valueOf(part), DIARY, String.valueOf(page));
    }

    public List<String> getDiaryText(int part, int page) {
        return super.getStringList(ExFile.toPath(getDiaryPagePath(part, page), DIARY_TEXT));
    }

    public String getDiaryDate(int part, int page) {
        return super.getString(ExFile.toPath(getDiaryPagePath(part, page), DIARY_DATE));
    }


    public String getActionType(int partId, int sectionId, int actionId) {
        return super.getString(ExFile.toPath(getActionPath(partId, sectionId, actionId), ACTION_TYPE));
    }

    public String getTriggerType(int partId, int sectionId, int actionId) {
        return super.getString(ExFile.toPath(getTriggerPath(partId, sectionId, actionId), TRIGGER_TYPE));
    }

}
