package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.user.event.UserRespawnEvent;
import de.timesnake.game.story.main.GameStory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionType;

public class UserManager implements Listener {

    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF, "§6Cooked Beef");
    public static final ExItemStack DRINK = new ExItemStack(Material.SPLASH_POTION, PotionType.WATER, false, false).setDisplayName("§6Water Bottle").hideAll();


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
    public void onUserRespawn(UserRespawnEvent e) {
        e.setRespawnLocation(((StoryUser) e.getUser()).getStoryRespawnLocation());
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        /*

        new BukkitRunnable() {

            @Override
            public void run() {
                ScoreboardTeam team = new ScoreboardTeam(null, "trest");

                ((CraftPlayer) p).getHandle().b.a(PacketPlayOutScoreboardTeam.a(team, true));


                GameProfile profile = new GameProfile(UUID.randomUUID(), "trest");
                MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
                WorldServer world = (((CraftWorld) Bukkit.getWorld("world")).getHandle());
                EntityPlayer player = new EntityPlayer(server, world, profile);

                player.fp().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU5MjM2NTY1NjUyOSwKICAicHJvZmlsZUlkIiA6ICI2MTI4MTA4MjU5M2Q0OGQ2OWIzMmI3YjlkMzIxMGUxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJuaWNyb25pYzcyMTk2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMGVmYmQwZmRjYzY2MDU4YTNiMzFiY2YxOWIwNzVhOGJmNWU3ZjU4ZDEyZWY3NGNlZjM5YjQ1ZDRiODllZWQiCiAgICB9CiAgfQp9", "KzWMkKcToaeYK3RJP8Yl8gBrFsfSTIdjjTdTc3OQH7zwGC08GJnZX+V9zESOshA7Bu0JMT5YCXxMmlGaNFvn6goxGmw46ZJoHO3NR3wRUD0ESImxWa/vgBuzAnkFEvwGnezl+jL/w6WmMgEbweP+eqFJx9RS+tYOkCvBpzn0O3+yXgmh/0KVV/2EKwZ/WgX1abMgd8S54MHWMRkiPVG3VD4DkzaQh7NligXh3qGS7uHvUfa4gvGGxVvYlatqypUHdxM62GgHVuR44JkHecmz6c/n2e80N0nugDYb0jyv89TUB4RwAJBbCrkMC1c6PIwUxqWyMyGKzLdu6DhuqPCviQyjYGdQbY3TLSZveqN94P/DtRD1svZ3BnXuxE4jhT0FhpElg0moUwt445Xns8EfKQ1ghGI0gZcJqgM2bLGeJoftgFAp6hych15R7GShYeThQ47m7sijrEvC9xD+A21mKC7NRFA+5EsMG6YriSJ8tcCMQxDMtAA7cYk3mD99tSwUwBC1jhoebm3KAlYXyPo+la5UHUcFh4FIjFhjU+JE8aVPOi3ZT991iYMluXSsCS4Rsw+hs6ic3cd4yyOT4eeoCDdb0l0uH+AVW0tibJ2F1d5IHbvfeNNwCv87lfC5NqviKQIn9w1uQ2rw8AhVngJm360VNblFtYMknp4rjVG35co="));

                player.o(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());

                ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, player));
                ((CraftPlayer) p).getHandle().b.a(PacketPlayOutScoreboardTeam.a(team, player.getBukkitEntity().getName(), PacketPlayOutScoreboardTeam.a.a));

                ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutNamedEntitySpawn(player));
                ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutEntityMetadata(player.getBukkitEntity().getEntityId(), player.ai(), true));

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, player));

                        player.o(p.getLocation().getBlockX(), p.getLocation().getBlockY() + 2, p.getLocation().getBlockZ());

                        ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutEntityTeleport(player));

                        final float[] yaw = {0};

                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutEntityHeadRotation(player, (byte) (yaw[0] * 256 / 360)));
                                ((CraftPlayer) p).getHandle().b.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(player.getBukkitEntity().getEntityId(), (byte) ((45 + yaw[0]) * 256 / 360), (byte) (0 * 256 / 360), true));

                                yaw[0] += 22.5;
                                System.out.println(yaw[0]);

                                if (yaw[0] >= 360) {
                                    yaw[0] = 0;
                                }
                            }
                        }.runTaskTimer(GameStory.getPlugin(), 20 * 2, 20 * 5);
                    }
                }.runTaskLater(GameStory.getPlugin(), 8);
            }
        }.runTaskLater(GameStory.getPlugin(), 20 * 5);

         */


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
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        StoryUser user = (StoryUser) Server.getUser(e.getPlayer());

        if (user.getExWorld().equals(user.getStoryWorld())) {
            if (user.getPart() != null) {
                Server.runTaskLaterSynchrony(() -> user.getPart().spawnCharacters(), 40, GameStory.getPlugin());
            }
        }
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
}
