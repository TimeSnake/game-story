package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.elements.InvalidQuestException;
import de.timesnake.game.story.user.StoryReader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class OptionalQuest extends Quest {

    private final Map<String, OptionalQuest> nextQuestByName;

    public OptionalQuest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
                         Map<String, OptionalQuest> nextQuestByName, StoryAction firstAction) {
        super(chapter, name, reader, startLocation, firstAction);
        this.nextQuestByName = nextQuestByName;
    }

    public OptionalQuest(Toml quest, String name, StoryAction firstAction) {
        super(quest, name, firstAction);
        this.nextQuestByName = new HashMap<>();
    }

    @Override
    public OptionalQuest clone(StoryChapter chapter, StoryReader reader) {
        Map<String, OptionalQuest> clonedNextQuests = new HashMap<>();
        for (OptionalQuest quest : this.nextQuestByName.values()) {
            clonedNextQuests.put(quest.getName(), quest.clone(chapter, reader));
        }
        return new OptionalQuest(chapter, this.name, reader, this.startLocation, clonedNextQuests, this.firstAction);
    }

    @Override
    public void forEachNext(Consumer<Quest> consumer) {
        consumer.accept(this);
        for (OptionalQuest quest : this.nextQuestByName.values()) {
            quest.forEachNext(consumer);
        }
    }

    @Override
    public Quest nextQuest() {
        for (OptionalQuest optionalQuest : this.nextQuestByName.values()) {
            optionalQuest.start(false, true);
        }
        return null;
    }

    @Override
    public Quest lastQuest() {
        return this.nextQuestByName.size() == 0 ? this : this.nextQuestByName.values().iterator().next();
    }

    @Override
    public void addNextQuest(Quest quest) {
        if (quest instanceof OptionalQuest) {
            this.nextQuestByName.put(quest.getName(), ((OptionalQuest) quest));
        } else if (quest instanceof MainQuest) {
            throw new InvalidQuestException("main-quest can not be a successor of a optional quest");
        } else {
            throw new InvalidQuestException("unknown quest type");
        }
    }
}
