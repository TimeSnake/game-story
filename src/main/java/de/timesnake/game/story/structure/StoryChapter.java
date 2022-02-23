package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.story.action.*;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.event.AreaEvent;
import de.timesnake.game.story.event.DropItemAtEvent;
import de.timesnake.game.story.event.DropItemEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class StoryChapter {

    private final Integer id;

    private final String name;

    private final ChapterFile file;
    private final Map<Integer, StoryPart> partsById = new HashMap<>();

    public StoryChapter(Integer id, ChapterFile file) {
        this.id = id;
        this.file = file;

        this.name = file.getChapterName();

        List<Integer> partIds = new ArrayList<>(this.file.getPartIds());
        partIds.sort(Integer::compareTo);

        for (Integer partId : partIds) {

            Server.printText(Plugin.STORY, "Loading part " + partId, "Chapter " + this.id);

            Map<Integer, StorySection> sectionsById = new HashMap<>();

            List<Integer> sectionIds = new ArrayList<>(this.file.getSectionIdsFromPart(partId));
            sectionIds.sort(Integer::compareTo);

            for (Integer sectionId : sectionIds) {

                Server.printText(Plugin.STORY, "Loading section " + sectionId, "Chapter " + this.id, "Part " + partId);

                LinkedList<Integer> actionIds = new LinkedList<>(this.file.getActionIdsFromSection(partId, sectionId));
                actionIds.sort(Integer::compareTo);

                if (actionIds.isEmpty()) {
                    break;
                }

                StoryAction first = null;
                try {
                    first = this.getActionFromFile(partId, sectionId, actionIds.getFirst());
                } catch (CharacterNotFoundException | ItemNotFoundException | UnknownLocationException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionIds.getFirst());
                }
                StoryAction previous = first;

                StringBuilder sb = new StringBuilder();
                sb.append("Actions: ").append(actionIds.getFirst());

                actionIds.removeFirst();

                for (Integer actionId : actionIds) {
                    sb.append(", ");

                    StoryAction action = null;
                    try {
                        action = this.getActionFromFile(partId, sectionId, actionId);
                    } catch (CharacterNotFoundException | ItemNotFoundException | UnknownLocationException e) {
                        Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
                    }

                    previous.setNext(action);
                    previous = action;

                    sb.append(actionId);

                }

                Server.printText(Plugin.STORY, sb.toString(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId);

                sectionsById.put(sectionId, new StorySection(file, partId, sectionId, first));
            }

            String partName = file.getPartName(partId);
            String partEndMessage = file.getPartEndMessage(partId);

            this.partsById.put(partId, new StoryPart(partId, partName, partEndMessage, sectionsById));
        }
    }

    private StoryAction getActionFromFile(Integer partId, Integer sectionId, Integer actionId) throws CharacterNotFoundException, ItemNotFoundException, UnknownLocationException {
        String type = this.file.getActionType(partId, sectionId, actionId);

        if (type == null) {
            type = TriggerAction.NAME;
        }

        String actionPath = ChapterFile.getActionPath(partId, sectionId, actionId);

        List<BaseComponent> diaryComponents = new ArrayList<>();
        List<String> diaryLines = this.file.getStringList(ExFile.toPath(actionPath, StoryAction.DIARY));

        if (diaryLines != null) {
            for (String line : diaryLines) {
                diaryComponents.add(new TextComponent(line));
            }
        }

        BaseComponent[] diaryPage = diaryComponents.toArray(new BaseComponent[0]);

        StoryAction action;

        switch (type.toLowerCase()) {
            case TalkAction.NAME:
                action = new TalkAction(actionId, diaryPage, this.file, actionPath);
                break;
            case ItemSearchAction.NAME:
                action = new ItemSearchAction(actionId, diaryPage, this.file, actionPath);
                break;
            case ItemGiveAction.NAME:
                action = new ItemGiveAction(actionId, diaryPage, this.file, actionPath);
                break;
            case DelayAction.NAME:
                action = new DelayAction(actionId, diaryPage, this.file, actionPath);
                break;
            case ThoughtAction.NAME:
                action = new ThoughtAction(actionId, diaryPage, this.file, actionPath);
                break;
            case ClearInventoryAction.NAME:
                action = new ClearInventoryAction(actionId, diaryPage);
                break;
            case ItemLootAction.NAME:
                action = new ItemLootAction(actionId, diaryPage, this.file, actionPath);
            case TriggerAction.NAME:
            default:
                action = new TriggerAction(actionId);

        }

        if (!(action instanceof TriggeredAction)) {
            return action;
        }

        String triggerPath = ChapterFile.getTriggerPath(partId, sectionId, actionId);

        if (!file.contains(triggerPath)) {
            return action;
        }

        TriggeredAction triggeredAction = ((TriggeredAction) action);

        String triggerType = file.getTriggerType(partId, sectionId, actionId);

        if (triggerType == null) {
            return action;
        }

        switch (triggerType.toLowerCase()) {
            case AreaEvent.NAME:
                triggeredAction.setTriggerEvent(new AreaEvent<>(triggeredAction, file, triggerPath));
                break;
            case DropItemAtEvent.NAME:
                triggeredAction.setTriggerEvent(new DropItemAtEvent<>(triggeredAction, file, triggerPath));
                break;
            case DropItemEvent.NAME:
                triggeredAction.setTriggerEvent(new DropItemEvent<>(triggeredAction, file, triggerPath));
                break;
            default:
                Server.printWarning(Plugin.STORY, "Unknown trigger type: " + triggerType, "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
        }

        return action;
    }

    public StoryPart getPart(int id) {
        return this.partsById.get(id);
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public StoryPart nextPart(StoryPart part) {
        return this.partsById.get(part.getId() + 1);
    }

    public Collection<StoryPart> getParts() {
        return this.partsById.values();
    }
}
