/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.action;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.game.story.element.StoryCharacter;
import de.timesnake.game.story.element.TalkType;
import de.timesnake.game.story.event.TriggerEvent;
import de.timesnake.game.story.exception.MissingArgumentException;
import de.timesnake.game.story.exception.StoryParseException;
import de.timesnake.game.story.exception.UnknownLocationException;
import de.timesnake.game.story.listener.StoryEvent;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBookBuilder;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.packets.core.packet.out.entity.ClientboundMoveEntityPacketBuilder;
import de.timesnake.library.packets.core.packet.out.entity.ClientboundRotateHeadPacketBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

public class TalkAction extends RadiusAction implements ChannelListener {

  public static final String MESSAGE_PLAYER = "p";
  public static final String MESSAGE_CHARACTER = "c";
  public static final String AUDIO = "a";

  public static final String NAME = "talk";
  private static final int MAX_ERROR_COUNT = 3;

  private final LinkedList<Tuple<Speaker, Supplier<String>>> messages;
  private final LinkedList<Tuple<Speaker, Supplier<String>>> audioMessages;

  private final StoryCharacter<?> speaker;
  private final Set<StoryUser> delayingByUser = new HashSet<>();
  private final float yaw;
  private final float pitch;
  private StoryUser partner;
  private Iterator<Tuple<Speaker, Supplier<String>>> messageIt;
  private Tuple<Speaker, Supplier<String>> currentMessage;
  private HoloDisplay display;

  private int errorCount = 0;


  public TalkAction(int id, StoryAction next, StoryCharacter<?> speaker,
                    LinkedList<Tuple<Speaker, Supplier<String>>> messages,
                    LinkedList<Tuple<Speaker, Supplier<String>>> audioMessages,
                    ExLocation location, StoryCharacter<?> character, Double radius, float yaw, float pitch) {
    super(id, next, location, character, radius);
    this.messages = messages;
    this.audioMessages = audioMessages;
    this.speaker = speaker;
    this.yaw = yaw;
    this.pitch = pitch;
  }

  public TalkAction(StoryBookBuilder bookBuilder, Quest quest, Toml action, int id,
                    List<Integer> diaryPages) throws StoryParseException {
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
        messageText = messageText.replaceFirst(MESSAGE_PLAYER + ":", "").trim();
        this.messages.add(new Tuple<>(Speaker.PLAYER, quest.parseString(messageText)));
      } else if (messageText.startsWith(MESSAGE_CHARACTER + ":")) {
        messageText = messageText.replaceFirst(MESSAGE_CHARACTER + ":", "").trim();
        this.messages.add(new Tuple<>(Speaker.CHARACTER, quest.parseString(messageText)));
      } else {
        this.logger.warn("Unknown speaker in action '{}'", id);
      }
    }

    List<Number> lookDirections = action.getList(CHARACTER_LOOK_DIRECTION);
    if (lookDirections.size() != 2) {
      throw new UnknownLocationException("invalid look direction");
    }
    this.yaw = lookDirections.get(0).floatValue();
    this.pitch = lookDirections.get(1).floatValue();

    this.audioMessages = new LinkedList<>();
    List<String> audioTexts = action.getList("audio");

    if (audioTexts != null) {
      for (String audioText : audioTexts) {
        if (audioText.startsWith(AUDIO + ":")) {
          audioText = audioText.replaceFirst(AUDIO + ":", "").trim();
          this.audioMessages.add(new Tuple<>(Speaker.AUDIO, quest.parseString(audioText)));
        } else if (audioText.startsWith(MESSAGE_PLAYER + ":")) {
          audioText = audioText.replaceFirst(MESSAGE_PLAYER + ":", "").trim();
          this.audioMessages.add(new Tuple<>(Speaker.PLAYER, quest.parseString(audioText)));
        }
      }
    }
  }

  @Override
  public StoryAction clone(Quest quest, StoryReader reader, StoryAction clonedNext,
                           StoryChapter chapter) {
    return new TalkAction(this.id, clonedNext,
        quest.getChapter().getCharacter(this.speaker.getName()),
        this.messages, this.audioMessages,
        this.location.clone().setExWorld(chapter.getWorld()),
        this.character != null ? quest.getChapter().getCharacter(this.character.getName()) : null,
        this.radius, this.yaw, this.pitch);
  }

  @Override
  public void start() {
    super.start();
    this.reader.forEach(u -> u.sendPacket(ClientboundRotateHeadPacketBuilder.of(this.speaker.getEntity(), this.yaw)));
    this.reader.forEach(u -> u.sendPacket(new ClientboundMoveEntityPacketBuilder(this.speaker.getEntity())
        .setRot(this.yaw >= 0 ? this.yaw + 44f : this.yaw - 44f, this.pitch, true)
        .build()));

  }

  private boolean isAudio() {
    return this.reader.getTalkType() == TalkType.AUDIO && this.audioMessages != null
           && !this.audioMessages.isEmpty();
  }

  @Override
  public void trigger(TriggerEvent.Type type, StoryUser user) {
    if (this.partner == null) {
      this.partner = user;
      if (this.isAudio()) {
        this.messageIt = this.audioMessages.listIterator();
      } else {
        this.messageIt = this.messages.listIterator();
      }
      this.nextMessage(user);

      Server.getChannel().addListener(this, Collections.singleton(this.partner.getUniqueId()));
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (this.display != null) {
      Server.getEntityManager().unregisterEntity(this.display);
    }
    this.partner = null;
    // Server.getChannel().removeListener(this);
  }

  private void nextMessage(StoryUser user) {
    if (this.display != null) {
      Server.getEntityManager().unregisterEntity(this.display);
    }

    user.resetTitle();

    if (!this.reader.containsUser(user)) {
      return;
    }

    if (!this.messageIt.hasNext()) {
      this.startNext();
      return;
    }

    this.currentMessage = this.messageIt.next();

    if (!this.isAudio()) {
      if (this.currentMessage.getA().equals(Speaker.CHARACTER)) {
        this.sendMessage(user, this.currentMessage.getB().get());
      } else {
        this.sendSelfMessage(user, this.currentMessage.getB().get());
      }
    } else {
      if (this.currentMessage.getA().equals(Speaker.AUDIO)) {
        Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(),
            MessageType.User.STORY_AUDIO_PLAY, this.currentMessage.getB().get()));
      } else {
        this.sendSelfMessage(user, this.currentMessage.getB().get());
      }
    }
  }

  private void sendSelfMessage(StoryUser user, String message) {
    user.showTitle(Component.empty(), Component.text(message), Duration.ofSeconds(20));
  }

  private void sendMessage(StoryUser user, String message) {
    List<String> lines = this.getMessageToLines(message);

    this.display = new HoloDisplay(this.location.clone().add(0, 0.8, 0), lines);

    Server.getEntityManager().registerEntity(this.display, user);

    Random random = new Random();

    if (this.character.isRotateable()) {
      Server.runTaskTimerAsynchrony((time) -> {
        if (time % 2 == 0) {
          user.sendPacket(ClientboundRotateHeadPacketBuilder.of(this.speaker.getEntity(),
              this.yaw - random.nextInt(10) + 8));
          user.sendPacket(new ClientboundMoveEntityPacketBuilder(this.speaker.getEntity())
              .setRot(this.yaw, this.pitch + random.nextInt(5) + 3, true)
              .build());
        } else {
          user.sendPacket(ClientboundRotateHeadPacketBuilder.of(this.speaker.getEntity(),
              this.yaw - random.nextInt(10) + 8));
          user.sendPacket(new ClientboundMoveEntityPacketBuilder(this.speaker.getEntity())
              .setRot(this.yaw, this.pitch - random.nextInt(5) - 3, true)
              .build());
        }

        if (time == 0) {
          user.sendPacket(ClientboundRotateHeadPacketBuilder.of(this.speaker.getEntity(),
              this.yaw));
          user.sendPacket(new ClientboundMoveEntityPacketBuilder(this.speaker.getEntity())
              .setRot(this.yaw, this.pitch, true)
              .build());
        }

      }, 8, true, 0, 7, GameStory.getPlugin());
    }
  }

  private List<String> getMessageToLines(String message) {
    List<String> lines = new ArrayList<>();

    String[] words = message.split(" ");

    int length = 0;

    StringBuilder line = new StringBuilder();

    for (String word : words) {
      if (length > 20) {
        lines.add(line.substring(0, line.length() - 1));
        line = new StringBuilder();
        length = 0;
      }

      line.append(word).append(" ");
      length += word.length();
    }

    lines.add(line.toString());

    return lines;
  }

  @StoryEvent
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
    if (!this.isActive()) {
      return;
    }

    if (!e.isSneaking()) {
      return;
    }

    StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

    if (user == null) {
      return;
    }

    if (!this.reader.containsUser(user)) {
      return;
    }

    if (this.reader.getTalkType().equals(TalkType.AUDIO)) {
      if (this.currentMessage != null && this.currentMessage.getA().equals(Speaker.AUDIO)) {
        return;
      }
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
    Server.runTaskLaterSynchrony(() -> this.delayingByUser.remove(user), 10,
        GameStory.getPlugin());

    if (this.partner.equals(user)) {
      this.nextMessage(user);
    }
  }

  @ChannelHandler(type = {ListenerType.USER_STORY_AUDIO_END, ListenerType.USER_STORY_AUDIO_FAIL})
  public void onStoryMessage(ChannelUserMessage<String> msg) {
    if (!this.isActive()) {
      return;
    }

    if (this.currentMessage != null && this.currentMessage.getB().get().equals(msg.getValue())) {
      this.logger.info("{} next audio, after {}", this.partner.getName(), msg.getValue());
      if (msg.getMessageType().equals(MessageType.User.STORY_AUDIO_END)) {
        this.errorCount = 0;
        this.nextMessage(this.partner);
      } else if (msg.getMessageType().equals(MessageType.User.STORY_AUDIO_FAIL)) {
        if (this.errorCount >= MAX_ERROR_COUNT) {
          this.partner.sendPluginTDMessage(StoryServer.PLUGIN, "§wError while loading audio, please connect an admin");
          this.nextMessage(this.partner);
          return;
        }

        this.partner.sendPluginTDMessage(StoryServer.PLUGIN, "§wLoading ...");
        Server.getChannel().sendMessage(new ChannelUserMessage<>(this.partner.getUniqueId(),
            MessageType.User.STORY_AUDIO_PLAY, this.currentMessage.getB().get()));

        this.errorCount++;
      }
    }
  }

  @Override
  public void despawnEntities() {
    if (this.display != null) {
      Server.getEntityManager().unregisterEntity(this.display);
      this.display = null;
    }

    this.reader.forEach(User::resetTitle);
  }

  @Override
  public Collection<String> getCharacterNames() {
    Collection<String> names = super.getCharacterNames();
    names.add(this.speaker.getName());
    return names;
  }

  public enum Speaker {
    PLAYER,
    CHARACTER,
    AUDIO
  }
}
