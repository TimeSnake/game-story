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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import java.util.HashSet;
import java.util.Set;

public class UserManager implements Listener, UserInventoryInteractListener {

    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF, "§6Cooked Beef").setDropable(false);
    public static final ExItemStack DRINK =
            ExItemStack.getPotion(Material.SPLASH_POTION, PotionType.WATER, false, false).setDisplayName("§6Water Bottle").hideAll().setDropable(false);
    public static final ExItemStack CHECKPOINT =
            new ExItemStack(Material.RED_DYE, "§cTeleport to last checkpoint").setDropable(false).setMoveable(false);

    public static final ExItemStack START_ITEM = new ExItemStack(Material.CLOCK).setDisplayName("§cStart")
            .setLore("", "§7Click to start").setDropable(false).setMoveable(false).immutable();

    private final Set<StoryUser> checkpointUsers = new HashSet<>();

    public UserManager() {
        Server.registerListener(this, GameStory.getPlugin());
        Server.getInventoryEventManager().addInteractListener(this, CHECKPOINT, START_ITEM);
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        ((StoryUser) e.getUser()).joinStoryHub();
    }

    @EventHandler
    public void onUserQuit(UserQuitEvent e) {
        ((StoryUser) e.getUser()).stopStory();
    }

    @EventHandler
    public void onUserRespawn(UserRespawnEvent e) {
        StoryUser user = (StoryUser) e.getUser();
        e.setRespawnLocation(user.getStoryRespawnLocation());

        if (user.getReaderGroup() != null) {
            user.getReaderGroup().addDeath();
        }
    }

    @EventHandler
    public void onUserDeath(UserDeathEvent e) {
        e.setAutoRespawn(true);
        e.setKeepInventory(true);
        e.getDrops().clear();
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (!FOOD.equals(ExItemStack.getItem(e.getItem(), false))) {
            return;
        }

        Server.runTaskLaterSynchrony(() -> {
            if (!user.contains(FOOD)) {
                user.addItem(FOOD);
            }
        }, 20, GameStory.getPlugin());

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof ThrownPotion)) {
            return;
        }

        if (!((ThrownPotion) e.getEntity()).getEffects().isEmpty()) {
            return;
        }

        User user = Server.getUser((Player) e.getEntity().getShooter());

        Server.runTaskLaterSynchrony(() -> {
            if (!user.contains(DRINK)) {
                user.addItem(DRINK);
            }
        }, 40, GameStory.getPlugin());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) {
            return;
        }

        if (Server.getUser(e.getPlayer()).isService()) {
            return;
        }

        if (e.getItem().getType().equals(Material.ENDER_EYE)) {
            if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {
                return;
            }
            e.setUseItemInHand(Event.Result.DENY);
        }
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent event) {
        StoryUser user = ((StoryUser) event.getUser());
        ExItemStack item = event.getClickedItem();

        if (item.equals(CHECKPOINT)) {
            if (this.checkpointUsers.contains(((StoryUser) event.getUser()))) {
                return;
            }

            this.checkpointUsers.add(user);

            user.getReaderGroup().getQuest().start(true, false);

            Server.runTaskLaterSynchrony(() -> this.checkpointUsers.remove(user), 2 * 20, GameStory.getPlugin());
        } else if (item.equals(START_ITEM)) {
            user.startStory();
        }
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        StoryUser user = ((StoryUser) e.getUser());
        StoryUser userDamager = ((StoryUser) e.getUserDamager());

        if (user.getSelectedUsers().contains(userDamager)) {
            if (!user.getJoinedUsers().contains(userDamager)) {
                user.getJoinedUsers().add(userDamager);
                user.sendPluginMessage(Plugin.STORY, Component.text("Added ", ExTextColor.PERSONAL)
                        .append(userDamager.getChatNameComponent()));
                userDamager.sendPluginMessage(Plugin.STORY, Component.text("Joined ", ExTextColor.PERSONAL)
                        .append(user.getChatNameComponent()));
            } else {
                user.getJoinedUsers().remove(userDamager);
                user.sendPluginMessage(Plugin.STORY, Component.text("Removed ", ExTextColor.PERSONAL)
                        .append(userDamager.getChatNameComponent()));
            }

        } else {
            if (!userDamager.getSelectedUsers().contains(user)) {
                userDamager.getSelectedUsers().add(user);
                userDamager.sendPluginMessage(Plugin.STORY, Component.text("Invited ", ExTextColor.PERSONAL)
                        .append(user.getChatNameComponent()));
                user.sendPluginMessage(Plugin.STORY, userDamager.getChatNameComponent()
                        .append(Component.text(" invited you. Hit to accept", ExTextColor.PERSONAL)));
            } else {
                userDamager.getSelectedUsers().remove(user);
                userDamager.sendPluginMessage(Plugin.STORY, Component.text("Removed ", ExTextColor.PERSONAL)
                        .append(user.getChatNameComponent()));
            }
        }
    }
}
