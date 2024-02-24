/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoryCmd implements CommandListener {

  private final Logger logger = LogManager.getLogger("story.cmd");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd,
                        Arguments<Argument> args) {

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
      String bookId = args.getString(0);
      String chapterName = args.getString(1);

      if (!user.getBoughtChapters(bookId).contains(chapterName)) {
        if (user.getCoins() < StoryServer.PART_PRICE) {
          sender.sendNotEnoughCoinsMessage(StoryServer.PART_PRICE - user.getCoins());
          return;
        }

        user.buyChapter(bookId, chapterName);
        sender.sendPluginMessage(Component.text("Bought chapter for ", ExTextColor.PERSONAL)
            .append(Component.text(StoryServer.PART_PRICE + " TimeCoins",
                ExTextColor.VALUE)));
        this.logger.info("{} bought chapter {}.{}", user.getName(), bookId, chapterName);
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
              .append(Component.text("can not play this chapter",
                  ExTextColor.WARNING)));
          return;
        }
      }

      user.prepareStoryChapter(bookId, chapterName);
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion()
        .addArgument(new Completion("add", "remove")
            .addArgument(Completion.ofPlayerNames()));
  }

  @Override
  public String getPermission() {
    return null;
  }
}
