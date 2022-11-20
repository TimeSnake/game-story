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

package de.timesnake.game.story.structure;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.story.action.StoryAction;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.exception.InvalidArgumentTypeException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract sealed class Quest implements Iterable<StoryAction> permits MainQuest, OptionalQuest {

    protected static final String START_LOCATION = "location";

    protected final String name;
    protected final ExLocation startLocation;
    protected StoryAction firstAction;
    protected StoryChapter chapter;

    protected StoryReader reader;
    protected String selectedQuest;

    protected boolean skip;
    protected Collection<Quest> questsToSkipAtStart = new LinkedList<>();
    protected Collection<Quest> questsToSkipAtEnd = new LinkedList<>();

    protected Map<String, Supplier<?>> varSupplier;

    public Quest(StoryChapter chapter, String name, StoryReader reader, ExLocation startLocation,
                 Map<String, Supplier<?>> varSupplier, StoryAction firstAction) {
        this.chapter = chapter;
        this.name = name;
        this.reader = reader;
        this.startLocation = startLocation.clone().setExWorld(chapter.getWorld());
        this.varSupplier = new HashMap<>();
        for (Map.Entry<String, Supplier<?>> entry : varSupplier.entrySet()) {
            Object value = entry.getValue().get();
            this.varSupplier.put(entry.getKey(), () -> value);
        }

        this.firstAction = firstAction.clone(this, reader, chapter);
    }

    public Quest(StoryBookBuilder bookBuilder, Toml quest, String name) throws InvalidArgumentTypeException {
        this.name = name;
        this.startLocation = ExLocation.fromList(quest.getList(START_LOCATION));
        this.varSupplier = new HashMap<>();

        if (quest.containsTable("var")) {
            for (Map.Entry<String, Object> entry : quest.getTable("var").entrySet()) {
                Supplier<?> supplier = this.parseVar(entry.getValue());
                if (supplier == null) {
                    throw new InvalidArgumentTypeException("Could not parse value of variable '" + entry.getKey() + "'");
                }
                this.varSupplier.put(entry.getKey(), supplier);
            }
        }
    }

    protected void setFirstAction(StoryAction firstAction) {
        this.firstAction = firstAction;
        this.firstAction.setQuest(this);
    }

    public abstract Quest clone(StoryChapter chapter, StoryReader reader, Map<String, Quest> visited);

    protected void cloneSkipQuests(Quest cloned, Map<String, Quest> visited) {
        for (Quest quest : this.questsToSkipAtStart) {
            cloned.questsToSkipAtStart.add(visited.get(quest.getName()));
        }

        for (Quest quest : this.questsToSkipAtEnd) {
            cloned.questsToSkipAtEnd.add(visited.get(quest.getName()));
        }
    }

    public abstract void forEachNext(Consumer<Quest> consumer, Set<Quest> visited);

    public String getName() {
        return name;
    }

    public StoryChapter getChapter() {
        return chapter;
    }

    public void setChapter(StoryChapter chapter) {
        this.forEachNext(q -> q.chapter = chapter, new HashSet<>());
    }

    public void start(boolean teleport, boolean spawnEntities) {
        if (this.skip) {
            this.nextQuest();
            return;
        }

        for (Quest quest : this.questsToSkipAtStart) {
            quest.skip();
        }

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

            }, 5, true, 0, 10, GameStory.getPlugin());

            Server.runTaskLaterSynchrony(() -> {
                this.reader.forEach(u -> u.lockLocation(false));
            }, 20 * 6, GameStory.getPlugin());
        }

        if (spawnEntities) {
            Server.runTaskLaterSynchrony(() -> {
                Server.printText(Plugin.STORY, Chat.listToString(this.reader.getUsers().stream()
                        .map(UserPlayerDelegation::getName).toList()) + " enabled quest '" + this.name + "'");
                int delay = 0;
                for (StoryAction action : this) {
                    Server.runTaskLaterSynchrony(action::spawnEntities, delay, GameStory.getPlugin());
                    delay += 10;
                }

                this.firstAction.start();
            }, 20, GameStory.getPlugin());
        }

    }

    public void end() {
        for (Quest quest : this.questsToSkipAtEnd) {
            quest.skip();
        }
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

    public Map<String, Supplier<?>> getVars() {
        return this.varSupplier;
    }

    public Supplier<Integer> parseAdvancedInt(Toml toml, String key) throws InvalidArgumentTypeException, MissingArgumentException {
        Object value = toml.toMap().get(key);
        if (value == null) {
            throw new MissingArgumentException(key);
        }
        return this.parseAdvancedInt(value);
    }

    public Supplier<Integer> parseAdvancedInt(Object value) throws InvalidArgumentTypeException {
        if (value instanceof Long) {
            return () -> ((Long) value).intValue();
        } else {
            String s = ((String) value);
            if (s.contains("..")) {
                String[] bounds = s.split("\\.\\.");
                int lower;
                int upper;
                try {
                    lower = Integer.parseInt(bounds[0]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return null;
                }

                try {
                    upper = Integer.parseInt(bounds[1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return null;
                }

                if (lower > upper) {
                    return null;
                }

                return () -> new Random().nextInt(lower, upper);
            } else if (s.contains(",")) {
                String[] values = s.split(",");
                int[] numbers = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    try {
                        numbers[i] = Integer.parseInt(values[i]);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return () -> numbers[new Random().nextInt(numbers.length)];
            } else if (s.startsWith("$")) {
                String varName = s.replaceFirst("\\$", "");
                if (!(this.varSupplier.get(varName).get() instanceof Integer)) {
                    throw new InvalidArgumentTypeException("Invalid var type");
                }
                return () -> ((Integer) this.varSupplier.get(varName).get());
            }
        }
        throw new InvalidArgumentTypeException("Invalid var type");
    }

    public Supplier<String> parseString(String value) {
        if (value == null) return null;
        if (value.isBlank()) return () -> value;

        String[] splitByVars = value.split("\\$");
        List<Object> result = new LinkedList<>();
        result.add(splitByVars[0]);
        for (int i = 1; i < splitByVars.length; i++) {
            String[] splitByBlank = splitByVars[i].split(" ", 2);
            result.add(this.getVars().get(splitByBlank[0]));
            result.add(" " + splitByBlank[1]);
        }
        return () -> {
            StringBuilder sb = new StringBuilder();
            for (Object obj : result) {
                if (obj instanceof String) {
                    sb.append(obj);
                } else if (obj instanceof Supplier<?>) {
                    sb.append(((Supplier<?>) obj).get());
                }
            }
            return sb.toString();
        };
    }

    public Supplier<?> parseVar(Object value) {
        if (value instanceof String) {
            Supplier<?> supplier;
            try {
                supplier = this.parseAdvancedInt(value);
                if (supplier != null) {
                    return supplier;
                }
            } catch (InvalidArgumentTypeException ignored) {
            }
        } else if (value instanceof Long) {
            return () -> ((Long) value).intValue();
        }
        return () -> value;
    }

    public abstract void addNextQuest(Quest quest);

    public abstract List<? extends Quest> getNextQuests();

    public void skip() {
        this.skip = true;
    }

    public void addQuestsToSkipAtStart(Quest quest) {
        this.questsToSkipAtStart.add(quest);
    }

    public void addQuestsToSkipAtEnd(Quest quest) {
        this.questsToSkipAtEnd.add(quest);
    }

    public enum Type {
        MAIN,
        OPTIONAL
    }
}
