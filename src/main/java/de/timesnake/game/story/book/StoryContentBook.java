package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.user.UserProgress;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StoryContentBook {

    private final ExItemStack item = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false).setMoveable(false);

    public StoryContentBook(UserProgress progress, StoryBook book) {
        Set<Integer> boughtChapters = progress.getBoughtChaptersByBook().get(book.getId());
        Map<Integer, String> questByChapter = progress.getQuestsByChaptersByBook().get(book.getId());

        Component chapterPage = Component.text(book.getName(), Style.style(TextDecoration.BOLD))
                .append(Component.newline());

        BookMeta meta = ((BookMeta) this.item.getItemMeta());

        for (StoryChapter chapter : book.getChapters()) {
            chapterPage = chapterPage.append(Component.newline()).append(Component.empty());

            String romanChapterId = "I".repeat(chapter.getId()).replace("IIIII", "V")
                    .replace("IIII", "IV").replace("VV", "X")
                    .replace("VIV", "IX");

            Optional<Integer> userMaxPartId = questByChapter.keySet().stream().max(Integer::compareTo);
            Integer userCurrentChapter = userMaxPartId.orElse(1);

            if (chapter.getId() <= userCurrentChapter) {
                if (!boughtChapters.contains(chapter.getId())) {
                    chapterPage = chapterPage
                            .append(Component.text(romanChapterId, Style.style(TextDecoration.BOLD))
                                    .append(Component.text(" " + chapter.getName()))
                                    .append(Component.text(" (" + StoryServer.PART_PRICE + " TC)", ExTextColor.GOLD))
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.text("Click to buy and play")))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/story " + book.getId() + " " + chapter.getId())));
                } else {
                    chapterPage = chapterPage
                            .append(Component.text(romanChapterId, Style.style(TextDecoration.BOLD))
                                    .append(Component.text(" " + chapter.getName()))
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.text("Click to play")))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/story " + book.getId() + " " + chapter.getId())));
                }
            } else {
                chapterPage = chapterPage
                        .append(Component.text(romanChapterId + " " + chapter.getName(), ExTextColor.DARK_GRAY));
            }
        }

        meta.addPages(chapterPage);
        meta.setAuthor("TimeSnake");
        meta.setTitle(book.getName());

        this.item.setItemMeta(meta);

    }

    public ExItemStack getItem() {
        return item;
    }
}
