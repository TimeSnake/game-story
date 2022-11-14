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

package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.TalkType;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;

public class StoryCmd implements CommandListener {

    private Code.Help talkTypeNotExists;

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
        } else if (args.getString(0).equalsIgnoreCase("talk")) {
            String typeString = args.getString(1).toUpperCase();
            TalkType type = TalkType.valueOf(typeString);

            if (type == null) {
                sender.sendMessageNotExist("talk-type", this.talkTypeNotExists, typeString);
                return;
            }

            if (user.getReaderGroup() == null) {
                sender.sendPluginMessage(Component.text("Can not set talk-type, no story selected", ExTextColor.WARNING));
                return;
            }

            boolean successful = user.getReaderGroup().setTalkType(type);
            if (successful) {
                user.sendPluginMessage(Plugin.STORY, Component.text("Updated talk type to ", ExTextColor.PERSONAL)
                        .append(Component.text(typeString, ExTextColor.VALUE)));
                user.sendPluginMessage(Plugin.STORY, Component.text("Click on start if done", ExTextColor.WARNING));
            }
        } else {
            if (!args.get(0).isInt(true)) {
                return;
            }

            Integer bookId = args.get(0).toInt();
            String chapterName = args.getString(1);

            if (!user.getBoughtChapters(bookId).contains(chapterName)) {
                if (user.getCoins() < StoryServer.PART_PRICE) {
                    sender.sendNotEnoughCoinsMessage(StoryServer.PART_PRICE - user.getCoins());
                    return;
                }

                user.buyChapter(bookId, chapterName);
                sender.sendPluginMessage(Component.text("Bought chapter for ", ExTextColor.PERSONAL)
                        .append(Component.text(StoryServer.PART_PRICE + " TimeCoins", ExTextColor.VALUE)));
                Server.printText(Plugin.STORY, user.getName() + " bought chapter " + bookId + "." + chapterName, "Buy");
            }

            if (!user.getProgress().canPlayChapter(bookId, chapterName)) {
                sender.sendPluginMessage(Component.text("You can not play this chapter", ExTextColor.WARNING));
                return;
            }

            StoryBook book = StoryServer.getBook(bookId);
            if (book == null) {
                sender.sendPluginMessage(Component.text("Unknown book", ExTextColor.WARNING));
                return;
            }

            StoryChapter chapter = book.getChapter(chapterName);

            if (chapter == null) {
                sender.sendPluginMessage(Component.text("Unknown chapter", ExTextColor.WARNING));
                return;
            }

            if (!chapter.getPlayerSizes().contains(user.getJoinedUsers().size() + 1)) {
                sender.sendPluginMessage(Component.text("Invalid player size ", ExTextColor.WARNING)
                        .append(Component.text(user.getJoinedUsers().size(), ExTextColor.VALUE)));
                return;
            }


            for (StoryUser member : user.getJoinedUsers()) {
                if (!member.getProgress().canPlayChapter(bookId, chapterName)) {
                    sender.sendPluginMessage(member.getChatNameComponent()
                            .append(Component.text("can not play this chapter", ExTextColor.WARNING)));
                    return;
                }
            }


            user.prepareStoryChapter(bookId, chapterName);
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
        this.talkTypeNotExists = plugin.createHelpCode("xnt", "talk-type not exists");
    }
}
