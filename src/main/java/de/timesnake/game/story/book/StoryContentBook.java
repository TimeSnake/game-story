package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.structure.StoryPart;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class StoryContentBook {

    private final ExItemStack book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false);

    public StoryContentBook(Map<Integer, Map<Integer, Integer>> sectionsByPartByChapter) {

        LinkedList<BaseComponent[]> pages = new LinkedList<>();

        LinkedList<BaseComponent> chapterPage = new LinkedList<>();


        BookMeta meta = ((BookMeta) this.book.getItemMeta());

        for (StoryChapter chapter : StoryServer.getChapters()) {

            TextComponent text;

            String romanChapterId = "I".repeat(chapter.getId()).replace("IIIII", "V").replace("IIII", "IV").replace("VV", "X").replace("VIV", "IX");

            if (sectionsByPartByChapter.get(chapter.getId()) != null) {
                text = new TextComponent(romanChapterId + ". " + chapter.getName() + "\n");
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to open chapter")));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(chapter.getId() + 1)));
            } else {
                text = new TextComponent("§8" + romanChapterId + ". " + chapter.getName() + "\n");
            }

            chapterPage.addLast(text);

            LinkedList<BaseComponent> partPage = new LinkedList<>();

            Map<Integer, Integer> sectionsByPart = sectionsByPartByChapter.get(chapter.getId());

            partPage.addLast(new TextComponent("§n§l" + romanChapterId + "§r§n " + chapter.getName() + "\n"));
            partPage.addLast(new TextComponent("\n"));

            Optional<Integer> userMaxPartId = sectionsByPart.keySet().stream().max(Integer::compareTo);
            Integer userCurrentPart = userMaxPartId.orElse(1);

            for (StoryPart part : chapter.getParts()) {
                TextComponent partText;

                if (part.getId() <= userCurrentPart) {
                    partText = new TextComponent(part.getId() + " " + part.getName() + "\n");

                    if (userCurrentPart.equals(part.getId())) {
                        partText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to start")));
                        partText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/story " + chapter.getId() + " " + part.getId()));
                    }
                } else {
                    partText = new TextComponent("§8" + part.getId() + " " + part.getName() + "\n");
                }

                partPage.addLast(partText);
            }

            pages.addLast(partPage.toArray(new BaseComponent[0]));


        }

        pages.addFirst(chapterPage.toArray(new BaseComponent[0]));


        meta.spigot().setPages(pages);
        meta.setAuthor("SchwertBallon, SirHoffelpoff");
        meta.setTitle("Story: Table of contents");

        this.book.setItemMeta(meta);

    }

    public ExItemStack getBook() {
        return book;
    }
}
