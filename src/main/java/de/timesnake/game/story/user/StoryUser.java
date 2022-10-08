/*
 * game-story.main
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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.database.util.Database;
import de.timesnake.game.story.book.StoryContentBook;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoryUser extends User {


    private final UserProgress progress;
    private final Map<Integer, StoryContentBook> contentBookByStoryId = new HashMap<>();

    private StoryReader readerGroup;

    private boolean playing = false;

    public StoryUser(Player player) {
        super(player);

        Tablist tablist = Server.getScoreboardManager().registerNewGroupTablist(this.getName(), Tablist.Type.DUMMY,
                DisplayGroup.MAIN_TABLIST_GROUPS, (e, t) -> {}, (e, t) -> {});

        tablist.setHeader("§6Time§2Snake§9.de");
        tablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \n" + Server.SUPPORT_EMAIL);

        tablist.addEntry(this);

        this.setTablist(tablist);


        this.progress = new UserProgress(Database.getStory().getUser(this.getUniqueId()));

        for (StoryBook book : StoryServer.getBooks()) {
            this.contentBookByStoryId.put(book.getId(), new StoryContentBook(this.progress, book));
        }
    }

    public StoryReader getReaderGroup() {
        return readerGroup;
    }

    public void setReaderGroup(StoryReader readerGroup) {
        this.readerGroup = readerGroup;
    }

    public void stopStory() {
        if (this.readerGroup != null) {
            if (this.readerGroup.getQuest() != null) {
                this.readerGroup.getQuest().clearEntities();
            }
            this.readerGroup.removeUser(this);
        }

        this.playing = false;
    }

    public void joinStoryHub() {
        this.setDefault();
        int slot = 0;
        for (StoryContentBook book : this.contentBookByStoryId.values()) {
            this.setItem(slot, book.getItem());
        }
        this.teleport(StoryServer.getBaseWorld());

        this.playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public Set<Integer> getBoughtChapters(Integer bookId) {
        return this.progress.getBoughtChaptersByBook().get(bookId);
    }

    public void buyChapter(int bookId, int chapterId) {
        this.removeCoins(StoryServer.PART_PRICE, true);
        this.progress.buyChapter(bookId, chapterId);
    }

    public ExLocation getStoryRespawnLocation() {
        return this.readerGroup.getQuest().getStartLocation();
    }

    @Deprecated
    public void startBookPart(int bookId, int chapterId) {
        this.readerGroup = new StoryReader(List.of(this));
        this.readerGroup.startBookChapter(bookId, chapterId);
    }
}
