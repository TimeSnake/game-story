package de.timesnake.game.story.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.story.book.StoryCmd;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.server.StoryServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameStory extends JavaPlugin {

    private static GameStory plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new StoryServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "story", new StoryCmd(), Plugin.STORY);

        StoryServerManager.getInstance().onStoryEnable();
    }

    public static GameStory getPlugin() {
        return plugin;
    }
}
