package de.timesnake.game.story.structure;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryUser;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class StoryPart {

    private final Integer id;

    private final String name;
    private final String endMessage;

    private final Map<Integer, StorySection> sectionsById;
    private final LinkedHashMap<Integer, StoryCharacter<?>> characterById = new LinkedHashMap<>();

    private final Diary diary;

    public StoryPart(Integer id, String name, String endMessage, Diary diary, Map<Integer, StorySection> sectionsById, Set<StoryCharacter<?>> characters) {
        this.id = id;
        this.name = name;
        this.endMessage = endMessage;
        this.diary = diary;
        this.sectionsById = sectionsById;

        for (StorySection section : this.sectionsById.values()) {
            section.setPart(this);
        }

        for (StoryCharacter<?> character : characters) {
            this.characterById.put(character.getId(), character);
        }

    }

    private StoryPart(StoryUser reader, Set<StoryUser> listeners, Integer id, String name, String endMessage, Diary diary, Map<Integer, StorySection> sectionsById, LinkedHashMap<Integer, StoryCharacter<?>> characterById) {
        this.id = id;
        this.name = name;
        this.endMessage = endMessage;
        this.diary = diary.clone(reader, listeners);

        for (StoryCharacter<?> character : characterById.values()) {
            this.characterById.put(character.getId(), character.clone(reader, listeners));
        }

        this.sectionsById = new LinkedHashMap<>();

        for (Map.Entry<Integer, StorySection> entry : sectionsById.entrySet()) {
            this.sectionsById.put(entry.getKey(), entry.getValue().clone(this, reader, listeners));
        }

    }

    public StoryPart clone(StoryUser reader, Set<StoryUser> listeners) {
        return new StoryPart(reader, listeners, this.id, this.name, this.endMessage, this.diary, this.sectionsById, this.characterById);
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

    public StorySection getLastSection() {
        return this.sectionsById.values().stream().max(Comparator.comparingInt(StorySection::getId)).orElse(this.getFirstSection());
    }

    public Diary getDiary() {
        return diary;
    }

    public void spawnCharacters() {
        int delay = 0;
        for (StoryCharacter<?> character : this.characterById.values()) {
            System.out.println(character.getName());
            Server.runTaskLaterAsynchrony(character::spawn, delay, GameStory.getPlugin());
            delay += 25;
        }
    }

    public void despawnCharacters() {
        this.characterById.values().forEach(StoryCharacter::despawn);
    }

    public StoryCharacter<?> getCharacter(Integer id) {
        return this.characterById.get(id);
    }
}
