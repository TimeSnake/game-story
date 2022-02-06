package de.timesnake.game.story.action;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.HoloDisplay;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityHeadRotation;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityLook;
import de.timesnake.game.story.elements.CharacterNotFoundException;
import de.timesnake.game.story.elements.StoryCharacter;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.*;

public class TalkAction extends RadiusAction implements EntityAction {

    public static final String NAME = "talk";

    private static final String CHARACTER = "character";
    private static final String MESSAGES = "messages";

    private static final String CHARACTER_LOOK_DIRECTION = "character_look_direction";
    private static final String YAW = "yaw";
    private static final String PITCH = "pitch";

    private final StoryCharacter<?> entity;
    private final List<String> messages;
    private HashMap<StoryUser, Integer> messageIndexbyUser = new HashMap<>();

    private final float yaw;
    private final float pitch;

    private Set<StoryUser> delayingByUser = new HashSet<>();

    private HoloDisplay display;

    public TalkAction(int id, BaseComponent[] diaryPage, StoryAction next, ExLocation location, double radius, StoryCharacter<?> entity, List<String> messages, float yaw, float pitch) {
        super(id, diaryPage, next, location, radius);
        this.messages = messages;
        this.entity = entity;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public TalkAction(int id, BaseComponent[] diaryPage, ChapterFile file, String actionPath) throws CharacterNotFoundException {
        super(id, diaryPage, false, file, actionPath);

        int charId = file.getActionValueInteger(actionPath, CHARACTER);
        this.entity = StoryServer.getCharater(charId);
        this.messages = file.getActionValueStringList(actionPath, MESSAGES);

        this.yaw = file.getActionValueDouble(actionPath, CHARACTER_LOOK_DIRECTION + "." + YAW).floatValue();
        this.pitch = file.getActionValueDouble(actionPath, CHARACTER_LOOK_DIRECTION + "." + PITCH).floatValue();
    }

    @Override
    public StoryAction clone(StoryUser reader, Set<StoryUser> listeners, StoryAction clonedNext) {
        return new TalkAction(this.id, this.diaryPage, clonedNext, this.location.clone().setExWorld(reader.getStoryWorld()), this.radius, this.entity.clone(reader, listeners), this.messages, this.yaw, this.pitch);
    }

    @Override
    protected void onUserNearby(StoryUser user) {
        if (this.messageIndexbyUser.get(user) == null) {
            this.messageIndexbyUser.put(user, 0);
            this.nextMessage(user);

            if (user.equals(this.reader)) {
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.entity.getEntity(), this.yaw, this.pitch, true));
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

        if (index % 2 == 0) {
            this.sendMessage(user, this.messages.get(index));
        } else {
            this.sendSelfMessage(user, this.messages.get(index));
        }
        this.messageIndexbyUser.put(user, index + 1);
    }

    private void sendMessageNothingToTell(StoryUser user) {
        if (this.display != null) {
            this.display.sendRemovePacketsTo(user);
            this.display = null;
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
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.entity.getEntity(), this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.entity.getEntity(), this.yaw, this.pitch + random.nextInt(5) + 3, true));
            } else {
                user.sendPacket(ExPacketPlayOutEntityHeadRotation.wrap(this.entity.getEntity(), this.yaw - random.nextInt(10) + 8));
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.entity.getEntity(), this.yaw, this.pitch - random.nextInt(5) - 3, true));
            }

            if (time == 0) {
                user.sendPacket(ExPacketPlayOutEntityLook.warp(this.entity.getEntity(), this.yaw, this.pitch, true));
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

        if (user.getLocation().distance(this.location) > this.radius) {
            return;
        }

        boolean delaying = this.delayingByUser.contains(user);

        if (delaying) {
            return;
        }

        this.delayingByUser.add(user);

        if (!this.isActive()) {
            this.sendMessageNothingToTell(user);
        } else {
            this.nextMessage(user);
        }

        Server.runTaskLaterSynchrony(() -> this.delayingByUser.remove(user), 10, GameStory.getPlugin());
    }

    @Override
    public void spawnEntities() {
        this.entity.spawn();
    }

    @Override
    public void despawnEntities() {
        this.entity.despawn();

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
