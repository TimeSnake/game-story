/*
 * timesnake.game-story.main
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

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public abstract sealed class Quest implements Iterable<StoryAction> permits MainQuest, OptionalQuest {

    protected static final String START_LOCATION = "location";

    protected final String name;
    protected final StoryAction firstAction;
    protected final ExLocation startLocation;

    protected StoryChapter chapter;

    protected StoryReader reader;
    protected String selectedQuest;

    public Quest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation, StoryAction firstAction) {
        this.chapter = chapter;
        this.name = name;
        this.reader = reader;
        this.startLocation = startLocation.clone().setExWorld(chapter.getWorld());
        this.firstAction = firstAction.clone(this, reader, chapter);
    }

    public Quest(Toml quest, String name, StoryAction firstAction) {
        this.name = name;
        this.startLocation = ExLocation.fromList(quest.getList(START_LOCATION));
        this.firstAction = firstAction;
        this.firstAction.setQuest(this);
    }

    public abstract Quest clone(StoryChapter chapter, StoryReader reader);

    public abstract void forEachNext(Consumer<Quest> consumer);

    public String getName() {
        return name;
    }

    public StoryChapter getChapter() {
        return chapter;
    }

    public void setChapter(StoryChapter chapter) {
        this.forEachNext(q -> q.chapter = chapter);
    }

    public void start(boolean teleport, boolean spawnEntities) {
        if (teleport) {
            this.reader.forEach(u -> u.teleport(this.startLocation));

            this.reader.forEach(u -> {
                u.addPotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1);
                u.lockLocation(true);
            });

            Server.runTaskTimerSynchrony((t) -> {

                for (int angle = 0; angle < 360; angle += 10) {
                    double x = (Math.sin(angle)) * 0.7;
                    double z = (Math.cos(angle)) * 0.7;

                    Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(102, 0, 102), 1.2f);
                    this.startLocation.getWorld().spawnParticle(Particle.REDSTONE, this.startLocation.getX() + x,
                            this.startLocation.getY(), this.startLocation.getZ() + z, 8, 0, 1.5, 0, 5, dust);
                }

            }, 8, true, 0, 10, GameStory.getPlugin());

            Server.runTaskLaterSynchrony(() -> {
                this.reader.forEach(u -> u.lockLocation(false));
            }, 20 * 6, GameStory.getPlugin());
        }

        if (spawnEntities) {
            Server.runTaskLaterSynchrony(() -> {
                Server.printText(Plugin.STORY, "Starting quest " + this.name + " [" +
                        Chat.listToString(this.reader.getUsers().stream().map(UserPlayerDelegation::getName).toList()) + "]");
                int delay = 0;
                for (StoryAction action : this) {
                    Server.runTaskLaterSynchrony(action::spawnEntities, delay, GameStory.getPlugin());
                    delay += 10;
                }

                this.firstAction.start();
            }, 20, GameStory.getPlugin());
        }

    }

    public void stop() {
        Server.runTaskLaterSynchrony(this::clearEntities, 10 * 20, GameStory.getPlugin());
    }

    public abstract Quest nextQuest();

    public abstract Quest lastQuest();

    public void setSelectedQuest(String name) {
        this.selectedQuest = name;
    }

    @Override
    public Iterator<StoryAction> iterator() {
        return new StoryAction.ActionIterator(this.firstAction);
    }

    public void clearEntities() {
        this.forEach(StoryAction::despawnEntities);
    }

    public ExLocation getStartLocation() {
        return this.startLocation;
    }

    public abstract void addNextQuest(Quest quest);

    public abstract List<? extends Quest> getNextQuests();

    public enum Type {
        MAIN,
        OPTIONAL
    }
}
