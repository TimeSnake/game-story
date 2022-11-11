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

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.TalkType;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.CharacterNotFoundException;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.UnknownLocationException;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityHeadRotation;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityLook;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.*;

public class TalkAction extends RadiusAction implements Listener {

    public static final String NAME = "talk";

    private final LinkedList<Tuple<Speaker, String>> messages;

    private final StoryCharacter<?> speaker;
    private final Set<StoryUser> delayingByUser = new HashSet<>();
    private final float yaw;
    private final float pitch;
    private final String audioName;
    private final int audioLength; // in seconds
    private StoryUser partner;
    private Integer messageIndex;
    private HoloDisplay display;


    public TalkAction(int id, StoryAction next, StoryCharacter<?> speaker, LinkedList<Tuple<Speaker, String>> messages,
                      ExLocation location, StoryCharacter<?> character, Double radius, float yaw, float pitch,
                      String audioName, int audioLength) {
        super(id, next, location, character, radius);
        this.messages = messages;
        this.speaker = speaker;
        this.yaw = yaw;
        this.pitch = pitch;
        this.audioLength = audioLength;
        this.audioName = audioName;

        Server.registerListener(this, GameStory.getPlugin());
    }

    public TalkAction(StoryBookBuilder bookBuilder, Toml action, int id, List<Integer> diaryPages) throws
            CharacterNotFoundException, UnknownLocationException, MissingArgumentException {
        super(bookBuilder, action, id, diaryPages);

        String charId = action.getString(CHARACTER);

        if (charId == null) {
            throw new MissingArgumentException("character");
        }

        this.speaker = bookBuilder.getCharacter(charId);

        this.messages = new LinkedList<>();
        List<String> messageTexts = action.getList(MESSAGES);

        if (messageTexts == null) {
            throw new MissingArgumentException("messages");
        }

        for (String messageText : messageTexts) {
            if (messageText.startsWith(MESSAGE_PLAYER + ":")) {
                messageText = messageText.replaceFirst(MESSAGE_PLAYER + ":", "");
                if (messageText.startsWith(" ")) {
                    messageText = messageText.replaceFirst(" ", "");
                }
                this.messages.add(new Tuple<>(Speaker.PLAYER, messageText));
            } else if (messageText.startsWith(MESSAGE_CHARACTER + ":")) {
                messageText = messageText.replaceFirst(MESSAGE_CHARACTER + ":", "");
                if (messageText.startsWith(" ")) {
                    messageText = messageText.replaceFirst(" ", "");
                }
                this.messages.add(new Tuple<>(Speaker.CHARACTER, messageText));
            } else {
                Server.printWarning(Plugin.STORY, "Unknown speaker in " + id, "Action");
            }
        }

        List<Number> lookDirections = action.getList(CHARACTER_LOOK_DIRECTION);
        if (lookDirections.size() != 2) {
            throw new UnknownLocationException("invalid look direction");
        }
        this.yaw = lookDirections.get(0).floatValue();
        this.pitch = lookDirections.get(1).floatValue();

        this.audioName = action.getString("audio_name");

        if (audioName != null) {
            Long audioLength = action.getLong("audio_length");
            if (audioLength == null) {
                throw new MissingArgumentException("audio_length");
            }
            this.audioLength = audioLength.intValue();
        } else {
            this.audioLength = 0;
        }
    }

    @Override
    public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext, StoryChapter chapter) {
        return new TalkAction(this.id, clonedNext, quest.getChapter().getCharacter(this.speaker.getName()), this.messages,
                this.location.clone().setExWorld(chapter.getWorld()),
                this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
                this.radius, this.yaw, this.pitch, this.audioName, this.audioLength);
    }

    @Override
    public void start() {
        super.start();
        this.reader.forEach(u -> u.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(), this.yaw)));
        this.reader.forEach(u -> u.sendPacket(ExPacketPlayOutEntityLook.wrap(this.speaker.getEntity(),
                this.yaw >= 0 ? this.yaw + 44f : this.yaw - 44f, this.pitch, true)));
    }

    private void playAudio() {
        if (this.audioName == null) {
            Server.printWarning(Plugin.STORY, "Missing audio name",
                    "Chapter " + this.getQuest().getChapter().getName(), "Quest " + this.getQuest().getName(),
                    "Action " + this.id);
            return;
        }

        // TODO play audio and wait
        this.reader.forEach(u -> {
            Server.getChannel().sendMessage(new ChannelUserMessage<>(u.getUniqueId(), MessageType.User.STORY_PLAY_AUDIO,
                    ""));
        });
    }

    private boolean isAudio() {
        return this.reader.getTalkType() == TalkType.AUDIO && this.audioName != null;
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (this.isAudio()) {
            this.playAudio();
        } else if (this.partner == null && this.reader.getTalkType() == TalkType.TEXT) {
            this.partner = user;
            this.messageIndex = 0;
            this.nextMessage(user);
        }
    }

    private void nextMessage(StoryUser user) {
        if (this.display != null) {
            Server.getEntityManager().unregisterEntity(this.display);
        }

        user.resetTitle();

        if (this.messageIndex >= this.messages.size() && this.reader.containsUser(user)) {
            this.startNext();
            return;
        }

        Tuple<Speaker, String> messageBySpeaker = this.messages.get(this.messageIndex);

        if (messageBySpeaker.getA().equals(Speaker.CHARACTER)) {
            this.sendMessage(user, messageBySpeaker.getB());
        } else {
            this.sendSelfMessage(user, messageBySpeaker.getB());
        }
        this.messageIndex++;
    }

    private void sendSelfMessage(StoryUser user, String message) {
        user.showTitle(Component.empty(), Component.text(message), Duration.ofSeconds(20));
    }

    private void sendMessage(StoryUser user, String message) {
        List<String> lines = this.getMessageToLines(message);

        this.display = new HoloDisplay(this.location.clone().add(0, 0.8, 0), lines);

        Server.getEntityManager().registerEntity(this.display, user);

        Random random = new Random();

        Server.runTaskTimerAsynchrony((time) -> {
            if (time % 2 == 0) {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(),
                        this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.wrap(this.speaker.getEntity(), this.yaw,
                        this.pitch + random.nextInt(5) + 3, true));
            } else {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(),
                        this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.wrap(this.speaker.getEntity(), this.yaw,
                        this.pitch - random.nextInt(5) - 3, true));
            }

            if (time == 0) {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(), this.yaw));
                user.sendPacket(ExPacketPlayOutEntityLook.wrap(this.speaker.getEntity(), this.yaw, this.pitch, true));
            }

        }, 8, true, 0, 7, GameStory.getPlugin());
    }

    private List<String> getMessageToLines(String message) {
        List<String> lines = new ArrayList<>();

        String[] words = message.split(" ");

        int length = 0;

        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (length > 20) {
                lines.add(line.toString());
                line = new StringBuilder();
                length = 0;
            }

            line.append(word).append(" ");
            length += word.length();
        }

        lines.add(line.toString());

        return lines;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) {
            return;
        }

        if (this.reader.getTalkType() != TalkType.TEXT) {
            return;
        }

        StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (!this.reader.containsUser(user)) {
            return;
        }

        boolean delaying = this.delayingByUser.contains(user);

        if (delaying) {
            return;
        }

        if (!this.location.getExWorld().equals(this.reader.getWorld())) {
            return;
        }

        if (user.getLocation().distanceSquared(this.location) > this.radius * this.radius) {
            return;
        }

        this.delayingByUser.add(user);

        if (this.isActive()) {
            if (this.partner.equals(user)) {
                this.nextMessage(user);
            }
        }

        Server.runTaskLaterSynchrony(() -> this.delayingByUser.remove(user), 10, GameStory.getPlugin());
    }

    @Override
    public void despawnEntities() {

        if (this.display != null) {
            Server.getEntityManager().unregisterEntity(this.display);
            this.display = null;
        }

        this.reader.forEach(UserPlayerDelegation::resetTitle);
    }

    @Override
    public Collection<String> getCharacterNames() {
        Collection<String> names = super.getCharacterNames();
        names.add(this.speaker.getName());
        return names;
    }

    private enum Speaker {
        PLAYER,
        CHARACTER
    }
}
