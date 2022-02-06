package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.game.story.main.GameStory;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class UserManager implements Listener {

    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF);

    public UserManager() {
        Server.registerListener(this, GameStory.getPlugin());
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
    public void onUserDeath(UserDeathEvent e) {
        StoryUser user = (StoryUser) e.getUser();

        user.stopStory();
        user.joinStoryHub();
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
}
