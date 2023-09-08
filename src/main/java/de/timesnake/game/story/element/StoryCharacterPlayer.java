/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.element;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.PacketPlayer;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.entity.PlayerBuilder;
import net.minecraft.world.entity.player.Player;

public class StoryCharacterPlayer extends StoryCharacter<Player> {

  public static final String NAME = "player";

  private static final String SKIN = "skin";
  private static final String SKIN_VALUE = "value";
  private static final String SKIN_SIGNATURE = "signature";


  private final String skinValue;
  private final String skinSignature;

  private boolean spawned = false;

  private PacketPlayer packetPlayer;

  public StoryCharacterPlayer(String name, String displayName, ExLocation location,
      String skinValue, String skinSignature) {
    super(name, displayName, location);
    this.skinValue = skinValue;
    this.skinSignature = skinSignature;
    this.entity = PlayerBuilder.ofName(name, skinValue, skinSignature)
        .applyOnEntity(e -> {
          e.setPos(this.getLocation().getX(), this.getLocation().getY(), this.getLocation().getZ());
        })
        .build();
  }

  public StoryCharacterPlayer(String name, Toml character) {
    super(name, character);

    this.skinValue = character.getString(SKIN + "_" + SKIN_VALUE);
    this.skinSignature = character.getString(SKIN + "_" + SKIN_SIGNATURE);
  }

  @Override
  public StoryCharacter<Player> clone(StoryReader reader, StoryChapter chapter) {
    StoryCharacterPlayer character = new StoryCharacterPlayer(this.name, this.displayName,
        this.location.clone().setExWorld(chapter.getWorld()), this.skinValue, this.skinSignature);
    character.reader = reader;
    return character;
  }

  @Override
  public boolean isRotateable() {
    return true;
  }

  @Override
  public void spawn() {
    if (this.spawned) {
      return;
    }

    this.spawned = true;

    this.packetPlayer = new PacketPlayer(this.entity, this.getLocation().clone());

    Server.getEntityManager().registerEntity(packetPlayer, this.reader.getUsers());
  }

  @Override
  public void despawn() {
    Server.getEntityManager().unregisterEntity(this.packetPlayer);
    Server.getEntityManager().unregisterEntity(this.packetPlayer);
  }
}
