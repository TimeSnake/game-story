package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

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

        if (args.getString(0).equalsIgnoreCase("add")) {
            if (!args.get(1).isPlayerName(true)) {
                return;
            }

            /*
            StoryUser listener = (StoryUser) args.get(1).toUser();
            boolean res = user.addListener(listener);
            if (res) {
                sender.sendPluginMessage(Component.text("Added player ", ExTextColor.PERSONAL)
                        .append(listener.getChatNameComponent()));
            } else {
                sender.sendPluginMessage(Component.text("Could not add player ")
                        .append(listener.getChatNameComponent()));
            }

             */
        } else if (args.getString(0).equalsIgnoreCase("remove")) {
            if (!args.get(1).isPlayerName(true)) {
                return;
            }
            /*
            StoryUser listener = (StoryUser) args.get(1).toUser();
            boolean res = user.removeListener(listener);
            if (res) {
                sender.sendPluginMessage(Component.text("removed player ", ExTextColor.PERSONAL)
                        .append(listener.getChatNameComponent()));
            } else {
                sender.sendPluginMessage(Component.text("Could not remove player ")
                        .append(listener.getChatNameComponent()));
            }

             */
        } else {
            if (!args.get(0).isInt(true) || !args.get(1).isInt(true)) {
                return;
            }

            Integer bookId = args.get(0).toInt();
            Integer chapterId = args.get(1).toInt();

            if (!user.getBoughtChapters(bookId).contains(chapterId)) {
                if (user.getCoins() < StoryServer.PART_PRICE) {
                    sender.sendNotEnoughCoinsMessage(StoryServer.PART_PRICE - user.getCoins());
                    return;
                }

                user.buyChapter(bookId, chapterId);
                sender.sendPluginMessage(Component.text("Bought chapter for ", ExTextColor.PERSONAL)
                        .append(Component.text(StoryServer.PART_PRICE + " TimeCoins", ExTextColor.VALUE)));
                Server.printText(Plugin.STORY, user.getName() + " bought chapter " + bookId + "." + chapterId, "Buy");
            }

            user.startBookPart(bookId, chapterId);
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("add", "remove");
        } else if (args.length() == 2) {
            if (args.get(0).equalsIgnoreCase("add") || args.get(0).equalsIgnoreCase("remove")) {
                return Server.getCommandManager().getTabCompleter().getPlayerNames();
            }
        }
        return List.of();
    }

    @Override
    public void loadCodes(de.timesnake.library.extension.util.chat.Plugin plugin) {

    }
}
