package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.HashSet;
import java.util.List;

public class StoryCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!sender.isPlayer(true)) {
            return;
        }

        StoryUser user = ((StoryUser) sender.getUser());

        if (!args.isLengthEquals(2, true)) {
            return;
        }

        if (!args.get(0).isInt(true) || !args.get(1).isInt(true)) {
            return;
        }

        Integer chapterId = args.get(0).toInt();
        Integer partId = args.get(1).toInt();

        if (!user.getBoughtParts(chapterId).contains(partId)) {
            if (user.getCoins() < StoryServer.PART_PRICE) {
                sender.sendNotEnoughCoinsMessage(StoryServer.PART_PRICE - user.getCoins());
                return;
            }

            user.buyPart(chapterId, partId);
            sender.sendPluginMessage(ChatColor.WARNING + "Bought part for " + ChatColor.VALUE + StoryServer.PART_PRICE + " TimeCoins");
            Server.printText(Plugin.STORY, user.getName() + " bought part " + chapterId + "." + partId, "Buy");
        }

        user.startChapterPart(chapterId, partId, new HashSet<>());
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
