package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.action.*;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.ItemNotFoundException;
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

        this.name = file.getName();

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

                StoryAction first = this.getActionFromFile(partId, sectionId, actionIds.getFirst());
                StoryAction previous = first;

                StringBuilder sb = new StringBuilder();
                sb.append("Actions: ").append(actionIds.getFirst());

                actionIds.removeFirst();

                for (Integer actionId : actionIds) {
                    sb.append(", ");

                    StoryAction action = this.getActionFromFile(partId, sectionId, actionId);

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

    private StoryAction getActionFromFile(Integer partId, Integer sectionId, Integer actionId) {
        String type = this.file.getActionType(partId, sectionId, actionId);

        if (type == null) {
            return null;
        }

        String actionPath = ChapterFile.getActionPath(partId, sectionId, actionId);

        List<BaseComponent> diaryComponents = new ArrayList<>();
        List<String> diaryLines = this.file.getActionValueStringList(actionPath, StoryAction.DIARY);

        if (diaryLines != null) {
            for (String line : diaryLines) {
                diaryComponents.add(new TextComponent(line));
            }
        }

        BaseComponent[] diaryPage = diaryComponents.toArray(new BaseComponent[0]);

        switch (type.toLowerCase()) {
            case TalkAction.NAME:
                try {
                    return new TalkAction(actionId, diaryPage, this.file, actionPath);
                } catch (CharacterNotFoundException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
                }
                break;
            case ItemSearchAction.NAME:
                try {
                    return new ItemSearchAction(actionId, diaryPage, this.file, actionPath);
                } catch (ItemNotFoundException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
                }
                break;
            case ItemTradeAction.NAME:
                try {
                    return new ItemTradeAction(actionId, diaryPage, this.file, actionPath);
                } catch (ItemNotFoundException | CharacterNotFoundException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
                }
                break;
            case ItemGiveAction.NAME:
                try {
                    return new ItemGiveAction(actionId, diaryPage, this.file, actionPath);
                } catch (CharacterNotFoundException | ItemNotFoundException e) {
                    Server.printWarning(Plugin.STORY, e.getMessage(), "Chapter " + this.id, "Part " + partId, "Section " + sectionId, "Action " + actionId);
                }
                break;
            case ThoughtAction.NAME:
                return new ThoughtAction(actionId, diaryPage, this.file, actionPath);
            case LocationSearchAction.NAME:
                return new LocationSearchAction(actionId, diaryPage, this.file, actionPath);

        }

        return null;
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
