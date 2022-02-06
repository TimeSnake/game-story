package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ThoughtAction extends StoryAction {

    public static final String NAME = "thought";

    private static final String MESSAGES = "messages";

    private final List<String> messages;
    private int messageIndex = 0;

    private boolean delaying = false;

    protected ThoughtAction(int id, BaseComponent[] diaryPage, StoryAction next, List<String> messages) {
        super(id, diaryPage, next);
        this.messages = messages;
    }

    public ThoughtAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) {
        super(id, diaryPage);

        this.messages = file.getActionValueStringList(actionPath, MESSAGES);
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listener, StoryAction clonedNext) {
        return new ThoughtAction(this.id, this.diaryPage, clonedNext, this.messages);
    }

    @Override
    public void start() {
        super.start();

        this.nextMessage();
    }

    private void nextMessage() {

        if (this.messageIndex >= this.messages.size()) {
            this.reader.resetTitle();
            this.startNext();
        }

        this.reader.resetTitle();
        for (StoryUser listener : this.listeners) {
            listener.resetTitle();
        }

        if (this.messageIndex < this.messages.size()) {
            this.reader.sendTitle("", this.messages.get(this.messageIndex), Duration.ofSeconds(20));
            for (StoryUser listener : this.listeners) {
                listener.sendTitle("", this.messages.get(this.messageIndex), Duration.ofSeconds(20));
            }
        }

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

        if (this.isActive()) {
            this.nextMessage();
        }

        Server.runTaskLaterSynchrony(() -> this.delaying = false, 20, GameStory.getPlugin());
    }
}
