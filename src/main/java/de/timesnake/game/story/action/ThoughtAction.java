package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.game.story.user.StoryUser;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ThoughtAction extends TriggeredAction implements Listener {

    public static final String NAME = "thought";

    private final List<String> messages;
    private int messageIndex = 0;

    private boolean delaying = false;

    protected ThoughtAction(int id, StoryAction next, List<String> messages) {
        super(id, next);
        this.messages = messages;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public ThoughtAction(int id, List<Integer> diaryPages, ChapterFile file, String actionPath) {
        super(id, diaryPages);

        this.messages = file.getStringList(ExFile.toPath(actionPath, MESSAGES));
    }

    @Override
    public StoryAction clone(StorySection section, StoryUser reader, Set<StoryUser> listener, StoryAction clonedNext) {
        return new ThoughtAction(this.id, clonedNext, this.messages);
    }

    private void nextMessage() {

        if (this.messageIndex >= this.messages.size()) {
            this.reader.resetTitle();
            this.startNext();
            return;
        }

        this.reader.resetTitle();
        for (StoryUser listener : this.listeners) {
            listener.resetTitle();
        }

        this.reader.showTitle(Component.empty(), Component.text(this.messages.get(this.messageIndex)), Duration.ofSeconds(20));
        this.listeners.forEach((u) -> u.showTitle(Component.empty(), Component.text(this.messages.get(this.messageIndex)), Duration.ofSeconds(20)));

        this.messageIndex++;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (!this.reader.equals(user)) {
            return;
        }

        if (this.delaying) {
            return;
        }

        this.delaying = true;


        if (this.isActive() && this.messageIndex > 0) {
            this.nextMessage();
        }

        Server.runTaskLaterSynchrony(() -> this.delaying = false, 20, GameStory.getPlugin());
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (this.messageIndex == 0) {
            this.nextMessage();
        }
    }
}
