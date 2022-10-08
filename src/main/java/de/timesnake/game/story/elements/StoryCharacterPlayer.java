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

package de.timesnake.game.story.elements;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.PacketPlayer;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.entities.entity.bukkit.ExPlayer;

public class StoryCharacterPlayer extends StoryCharacter<ExPlayer> {

    public static final String NAME = "player";

    private static final String SKIN = "skin";
    private static final String SKIN_VALUE = "value";
    private static final String SKIN_SIGNATURE = "signature";


    private final String skinValue;
    private final String skinSignature;

    private boolean spawned = false;

    private PacketPlayer packetPlayer;

    public StoryCharacterPlayer(String name, String displayName, ExLocation location, String skinValue, String skinSignature) {
        super(name, displayName, location);
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;

        this.entity.setTextures(this.skinValue, this.skinSignature);
    }

    public StoryCharacterPlayer(String name, Toml character) {
        super(name, character);

        this.skinValue = character.getString(SKIN + "_" + SKIN_VALUE);
        this.skinSignature = character.getString(SKIN + "_" + SKIN_SIGNATURE);
    }

    @Override
    public StoryCharacter<ExPlayer> clone(StoryReader reader, StoryChapter chapter) {
        StoryCharacterPlayer character = new StoryCharacterPlayer(this.name, this.displayName,
                this.location.clone().setExWorld(chapter.getWorld()), this.skinValue, this.skinSignature);
        character.reader = reader;
        return character;
    }

    @Override
    protected ExPlayer initEntity() {
        ExPlayer player = new ExPlayer(this.location.getWorld(), this.displayName);

        player.setPosition(this.location.getX(), this.location.getY(), this.location.getZ());

        return player;
    }

    @Override
    public void spawn() {
        if (this.spawned) {
            return;
        }

        this.spawned = true;

        this.packetPlayer = new PacketPlayer(this.entity, new ExLocation(Server.getWorld(this.entity.getWorld()),
                this.entity.getLocation()));

        Server.getEntityManager().registerEntity(packetPlayer, this.reader.getUsers());
    }

    @Override
    public void despawn() {
        Server.getEntityManager().unregisterEntity(this.packetPlayer);
        Server.getEntityManager().unregisterEntity(this.packetPlayer);
    }
}
