/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.UserProgress;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Set;

public class StoryContentBook {

  private final ExItemStack item = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false)
      .setMoveable(false);

  public StoryContentBook(UserProgress progress, StoryBook book) {
    Set<String> boughtChapters = progress.getBoughtChaptersByBook().get(book.getId());

    Component mainPage = Component.text(book.getTitle()).decorate(TextDecoration.BOLD)
        .append(Component.newline());

    BookMeta meta = ((BookMeta) this.item.getItemMeta());

    int chapterIndex = 1;
    for (StoryChapter chapter : book.getChapters()) {
      mainPage = mainPage.append(Component.newline());

      String romanChapterId = "I".repeat(chapterIndex).replace("IIIII", "V")
          .replace("IIII", "IV").replace("VV", "X")
          .replace("VIV", "IX");

      if (progress.canPlayChapter(book.getId(), chapter.getId())) {
        mainPage = mainPage
            .append(Component.text(romanChapterId).decorate(TextDecoration.BOLD))
            .append(Component.text(" " + chapter.getTitle())
                .append(Component.text(" 웃"))
                .append(Chat.listToComponent(chapter.getPlayerSizes()))
                .decoration(TextDecoration.BOLD, false));

        if (!boughtChapters.contains(chapter.getId())) {
          mainPage = mainPage
              .append(Component.text(" (" + StoryServer.PART_PRICE + " TC)", ExTextColor.GOLD)
                  .decoration(TextDecoration.BOLD, false))
              .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                  Component.text("Click to buy and play")))
              .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                  "/story " + book.getId() + " " + chapter.getId()));
        } else {
          mainPage = mainPage
              .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                  Component.text("Click to play")))
              .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                  "/story " + book.getId() + " " + chapter.getId()));
        }
      } else {
        mainPage = mainPage
            .append(Component.text(romanChapterId, Style.style(TextDecoration.BOLD)))
            .append(Component.text(" " + chapter.getTitle(), ExTextColor.DARK_GRAY)
                .append(Component.text(" [", ExTextColor.DARK_GRAY))
                .append(Chat.listToComponent(chapter.getPlayerSizes(), ExTextColor.DARK_GRAY,
                    ExTextColor.DARK_GRAY))
                .append(Component.text("]", ExTextColor.DARK_GRAY))
                .decoration(TextDecoration.BOLD, false));
      }
      chapterIndex++;
    }

    meta.addPages(mainPage);
    meta.setAuthor("TimeSnake");
    meta.setTitle(book.getTitle());

    this.item.setItemMeta(meta);

  }

  public ExItemStack getItem() {
    return item;
  }
}
