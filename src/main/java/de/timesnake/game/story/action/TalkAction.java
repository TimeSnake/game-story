package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.HoloDisplay;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityHeadRotation;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityLook;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.elements.UnknownLocationException;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Tuple;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.*;

public class TalkAction extends RadiusAction implements Listener {

    public static final String NAME = "talk";

    private final LinkedList<Tuple<Speaker, String>> messages;

    private final StoryCharacter<?> speaker;
    private final HashMap<StoryUser, Integer> messageIndexbyUser = new HashMap<>();
    private final Set<StoryUser> delayingByUser = new HashSet<>();

    private final float yaw;
    private final float pitch;

    public TalkAction(int id, BaseComponent[] diaryPage, StoryAction next, StoryCharacter<?> speaker, LinkedList<Tuple<Speaker, String>> messages, ExLocation location, StoryCharacter<?> character, Double radius, float yaw, float pitch) {
        super(id, diaryPage, next, location, character, radius);
        this.messages = messages;
        this.speaker = speaker;
        this.yaw = yaw;
        this.pitch = pitch;

        Server.registerListener(this, GameStory.getPlugin());
    }

    private HoloDisplay display;

    public TalkAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException, UnknownLocationException {
        super(id, diaryPage, file, actionPath);

        int charId = file.getInt(ExFile.toPath(actionPath, CHARACTER));
        this.speaker = StoryServer.getCharater(charId);

        this.messages = new LinkedList<>();
        List<?> messageTexts = file.getList(ExFile.toPath(actionPath, MESSAGES));

        for (Object messageText : messageTexts) {
            try {
                LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) messageText;

                if (map.containsKey(MESSAGE_PLAYER)) {
                    this.messages.addLast(new Tuple<>(Speaker.PLAYER, map.get(MESSAGE_PLAYER)));
                } else if (map.containsKey(MESSAGE_CHARACTER)) {
                    this.messages.addLast(new Tuple<>(Speaker.CHARACTER, map.get(MESSAGE_CHARACTER)));
                } else {
                    Server.printWarning(Plugin.STORY, "Unknown speaker in " + actionPath, "Action");
                }
            } catch (ClassCastException e) {
                Server.printWarning(Plugin.STORY, "Missing speaker type in " + actionPath, "Action");
            }
        }

        this.yaw = file.getDouble(ExFile.toPath(actionPath, CHARACTER_LOOK_DIRECTION, YAW)).floatValue();
        this.pitch = file.getDouble(ExFile.toPath(actionPath, CHARACTER_LOOK_DIRECTION, PITCH)).floatValue();
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new TalkAction(this.id, this.diaryPage, clonedNext, this.speaker.clone(reader, listeners), this.messages, this.location.clone().setExWorld(reader.getStoryWorld()), this.character != null ? this.character.clone(reader, listeners) : null, this.radius, this.yaw, this.pitch);
    }

    @Override
    public void trigger(TriggerEvent.Type type, StoryUser user) {
        if (!this.messageIndexbyUser.containsKey(user)) {
            this.messageIndexbyUser.put(user, 0);
            this.nextMessage(user);

            if (user.equals(this.reader)) {
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.speaker.getEntity(), this.yaw, this.pitch, true));
            }
        }
    }

    private void nextMessage(StoryUser user) {
        if (this.display != null) {
            this.display.remove();
        }

        user.resetTitle();

        Integer index = this.messageIndexbyUser.get(user);

        if (index >= this.messages.size() && this.reader.equals(user)) {
            this.startNext();
            return;
        }

        Tuple<Speaker, String> messageBySpeaker = this.messages.get(index);

        if (messageBySpeaker.getA().equals(Speaker.CHARACTER)) {
            this.sendMessage(user, messageBySpeaker.getB());
        } else {
            this.sendSelfMessage(user, messageBySpeaker.getB());
        }
        this.messageIndexbyUser.put(user, index + 1);
    }

    private enum Speaker {
        PLAYER, CHARACTER
    }

    private void sendMessageNothingToTell(StoryUser user) {
        if (this.display != null) {
            this.display.remove();
        }

        user.resetTitle();

        String text = "";
        switch (new Random().nextInt(3)) {
            case 0:
                text = "Ich habe dir nichts mehr zu sagen";
                break;
            case 1:
                text = "Ich kann dir nicht weiter helfen";
                break;
            case 2:
                text = "...";
                break;
        }

        this.sendMessage(user, text);
    }

    private void sendSelfMessage(StoryUser user, String message) {
        user.sendTitle("", message, Duration.ofSeconds(20));
    }

    private void sendMessage(StoryUser user, String message) {
        List<String> lines = this.getMessageToLines(message);

        this.display = new HoloDisplay(this.location.clone().add(0, 0.8, 0), lines);

        this.display.addWatcher(user);

        Random random = new Random();

        Server.runTaskTimerAsynchrony((time) -> {
            if (time % 2 == 0) {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(), this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.speaker.getEntity(), this.yaw, this.pitch + random.nextInt(5) + 3, true));
            } else {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.speaker.getEntity(), this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.speaker.getEntity(), this.yaw, this.pitch - random.nextInt(5) - 3, true));
            }

            if (time == 0) {
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.speaker.getEntity(), this.yaw, this.pitch, true));
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

        StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (!this.reader.equals(user) && !this.listeners.contains(user)) {
            return;
        }

        boolean delaying = this.delayingByUser.contains(user);

        if (delaying) {
            return;
        }

        if (user.getLocation().distanceSquared(this.location) > this.radius * this.radius) {
            return;
        }

        this.delayingByUser.add(user);

        if (this.isActive()) {
            if (this.messageIndexbyUser.containsKey(user)) {
                this.nextMessage(user);
            }
        }

        Server.runTaskLaterSynchrony(() -> this.delayingByUser.remove(user), 10, GameStory.getPlugin());
    }

    @Override
    public void spawnEntities() {
        this.speaker.spawn();
    }

    @Override
    public void despawnEntities() {
        this.speaker.despawn();

        if (this.display != null) {
            this.display.sendRemovePacketsTo(this.reader);
            this.display.sendRemovePacketsTo(this.listeners);
            this.display = null;
        }

        this.reader.resetTitle();

        for (StoryUser listener : this.listeners) {
            listener.resetTitle();
        }
    }
}
