package de.timesnake.game.story.structure;

import de.timesnake.game.story.user.StoryUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StoryPart {

    private final Integer id;

    private final String name;
    private final String endMessage;

    private final Map<Integer, StorySection> sectionsById;

    public StoryPart(Integer id, String name, String endMessage, Map<Integer, StorySection> sectionsById) {
        this.id = id;
        this.name = name;
        this.endMessage = endMessage;
        this.sectionsById = sectionsById;
    }

    public StoryPart clone(StoryUser reader, Set<StoryUser> listeners) {
        Map<Integer, StorySection> sections = new HashMap<>();
        for (Map.Entry<Integer, StorySection> entry : this.sectionsById.entrySet()) {
            sections.put(entry.getKey(), entry.getValue().clone(reader, listeners));
        }
        return new StoryPart(this.id, this.name, this.endMessage, sections);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public StorySection getSection(int id) {
        return this.sectionsById.get(id);
    }

    public StorySection nextSection(StorySection section) {
        return this.sectionsById.get(section.getId() + 1);
    }

    public StorySection getFirstSection() {
        return this.sectionsById.get(1);
    }
}
