package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.game.story.main.GameStory;
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

    private final Set<StoryUser> checkpointUsers = new HashSet<>();

    public UserManager() {
        Server.registerListener(this, GameStory.getPlugin());
        Server.getInventoryEventManager().addInteractListener(this, CHECKPOINT);
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
        e.setRespawnLocation(((StoryUser) e.getUser()).getStoryRespawnLocation());
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
        if (this.checkpointUsers.contains(((StoryUser) event.getUser()))) {
            return;
        }

        this.checkpointUsers.add(((StoryUser) event.getUser()));

        ((StoryUser) event.getUser()).getReaderGroup().getQuest().start(true, false);

        Server.runTaskLaterSynchrony(() -> this.checkpointUsers.remove(((StoryUser) event.getUser())), 2 * 20,
                GameStory.getPlugin());
    }
}
