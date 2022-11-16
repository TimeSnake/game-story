/*
 * workspace.game-story.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.TalkType;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.player.UserList;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class StoryReader implements Iterable<StoryUser> {

    private final UUID id = UUID.randomUUID();

    private final StoryUser host;
    private final UserList<StoryUser> users;
    private TalkType talkType = TalkType.TEXT;

    private StoryBook book;
    private StoryChapter chapter;
    private Quest quest;

    private boolean perfomedPreChecks = false;

    public StoryReader(StoryUser host, Collection<StoryUser> users) {
        this.host = host;
        this.users = new UserList<>(users);
    }

    public UUID getId() {
        return id;
    }

    public TalkType getTalkType() {
        return talkType;
    }

    public boolean setTalkType(TalkType talkType) {
        if (!this.perfomedPreChecks) {
            this.talkType = talkType;
            return true;
        }
        return false;
    }

    public boolean matchAnyUser(Predicate<StoryUser> predicate) {
        return this.users.stream().anyMatch(predicate);
    }

    public List<StoryUser> filterUsers(Predicate<StoryUser> predicate) {
        return this.users.stream().filter(predicate).toList();
    }

    public List<StoryUser> getUsers() {
        return this.users;
    }

    public boolean containsUser(StoryUser user) {
        return this.users.contains(user);
    }

    public StoryUser anyUser() {
        return this.users.stream().findAny().get();
    }

    public void removeUser(StoryUser user) {
        this.users.remove(user);

        if (this.getUsers().isEmpty()) {
            this.destroy();
        } else {
            this.saveProgress();
        }
    }

    public ExWorld getWorld() {
        if (this.chapter != null) {
            return this.chapter.getWorld();
        }
        return null;
    }

    public void onCompletedQuest(Quest quest) {
        quest.stop();
        Server.printText(Plugin.STORY, Chat.listToString(this.users.stream()
                .map(UserPlayerDelegation::getName).toList()) + " completed '" + quest.getName() + "'");

        if (!quest.equals(this.quest)) {
            quest.nextQuest();
            return;
        }

        Quest next = this.quest.nextQuest();
        if (next == null) {
            this.onCompletedChapter();
            return;
        }

        Quest previous = this.quest;
        this.quest = next;

        previous.clearEntities();
        this.saveProgress();

        this.quest.start(false, true);
    }

    private void onCompletedChapter() {
        this.forEach(u -> u.showTitle(Component.empty(), Component.text(this.chapter.getEndMessage()), Duration.ofSeconds(3)));

        // TODO save progress

        Server.runTaskLaterSynchrony(() -> {
            this.forEach(StoryUser::joinStoryHub);
            this.chapter.despawnCharacters();
        }, 5 * 20, GameStory.getPlugin());
    }

    public void setSelectedQuest(String name) {
        this.quest.setSelectedQuest(name);
    }

    @NotNull
    @Override
    public Iterator<StoryUser> iterator() {
        return this.users.iterator();
    }

    public void startBookChapter(Integer bookId, String chapterName) {
        if (!this.perfomedPreChecks) {
            if (this.runPreChecks()) {
                return;
            }
        }

        StoryBook book = StoryServer.getBook(bookId);

        if (book == null) {
            return;
        }

        StoryChapter chapter = book.getChapter(chapterName);

        if (chapter == null) {
            return;
        }

        this.book = book;
        this.chapter = chapter.clone(this);

        String savedQuestName = this.host.getProgress().getQuest(bookId, chapterName);
        if (savedQuestName != null) {
            this.quest = this.chapter.getQuest(savedQuestName);
        } else {
            this.quest = this.chapter.getFirstQuest();
        }

        // TODO update progress

        this.quest = this.chapter.getFirstQuest();

        // TODO update progress

        ExItemStack diary = this.chapter.getDiary().getBook();

        this.forEach(u -> {
            u.clearInventory();
            u.setGameMode(GameMode.SURVIVAL);

            u.setItem(0, diary);
            u.setItem(1, UserManager.FOOD);
            u.setItem(2, UserManager.DRINK);
            u.setItem(8, UserManager.CHECKPOINT);

            u.setPlaying(true);
        });

        this.chapter.spawnCharacters();
        this.quest.start(true, true);
    }

    private boolean runPreChecks() {
        boolean checksDone = false;

        if (this.talkType == TalkType.AUDIO) {
            this.forEach(u -> u.sendPluginMessage(Plugin.STORY,
                    Component.text("Login to our website and start the audio check now. ", ExTextColor.PERSONAL)
                            .append(Component.text("https://timesnake.de/story/interface/?story=", ExTextColor.VALUE))));
            checksDone = true;
        }

        if (checksDone) {
            this.host.sendPluginMessage(Plugin.STORY, Component.text("Click on the start item if done", ExTextColor.WARNING));
        }

        this.perfomedPreChecks = true;
        return checksDone;
    }

    public void destroy() {
        this.saveProgress();
        if (this.quest != null) {
            this.quest.clearEntities();
        }

        this.book = null;
        this.chapter = null;
        this.quest = null;

        Server.getWorldManager().deleteWorld(this.getWorld(), true);
    }

    public void saveProgress() {
        // TODO save progress from current quest
    }

    public StoryBook getBook() {
        return book;
    }

    public StoryChapter getChapter() {
        return chapter;
    }

    public Quest getQuest() {
        return quest;
    }
}
