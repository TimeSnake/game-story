package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class Diary {

    private final ExItemStack book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false);

    private final HashMap<Integer, BaseComponent[]> pagesByNumber;
    private StoryUser reader;
    private Set<StoryUser> listeners;
    private Set<Integer> writtenPages = new HashSet<>();

    public Diary(StoryUser reader, Set<StoryUser> listeners, HashMap<Integer, BaseComponent[]> pagesByNumber) {
        this.reader = reader;
        this.listeners = listeners;
        this.pagesByNumber = pagesByNumber;
    }

    public Diary(ChapterFile chapterFile, int partId) {
        this.pagesByNumber = new HashMap<>();

        for (Integer pageNumber : chapterFile.getPathIntegerList(chapterFile.getDiaryPath(partId))) {
            List<String> text = chapterFile.getDiaryText(partId, pageNumber);

            BaseComponent[] components = new BaseComponent[text.size()];

            for (int i = 0; i < text.size(); i++) {
                components[i] = new TextComponent(text.get(i) + "\n");
            }

            this.pagesByNumber.put(pageNumber, components);
        }

        BookMeta meta = ((BookMeta) this.book.getItemMeta());

        if (!meta.hasPages()) {
            meta.spigot().setPages(new BaseComponent[]{});
        }

        meta.setAuthor("Yourself");
        meta.setTitle("Diary");

        this.book.setItemMeta(meta);
    }

    public Diary clone(StoryUser reader, Set<StoryUser> listeners) {
        return new Diary(reader, listeners, pagesByNumber);
    }

    public void loadPage(Integer... pageNumbers) {
        BookMeta meta = (BookMeta) this.book.getItemMeta();
        meta.spigot().setPages();

        this.writtenPages.addAll(Arrays.asList(pageNumbers));

        int pages = this.writtenPages.size() > 0 ? Collections.max(this.writtenPages) : 1;

        for (int page = 1; page <= pages; page++) {
            BaseComponent[] text = this.pagesByNumber.get(page);

            if (text != null && this.writtenPages.contains(page)) {
                meta.spigot().addPage(text);
            } else {
                meta.spigot().addPage(new BaseComponent[]{});
            }
        }

        if (!meta.hasPages()) {
            meta.spigot().setPages(new BaseComponent[]{});
        }

        meta.setAuthor("Yourself");
        meta.setTitle("Diary");

        this.book.setItemMeta(meta);

        this.reader.setItem(0, this.book);

        this.listeners.forEach(u -> u.setItem(0, this.book));
    }

    public ExItemStack getBook() {
        return book;
    }
}
