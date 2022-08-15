package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.structure.ChapterFile;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class Diary {

    private final ExItemStack book;

    private final HashMap<Integer, BaseComponent[]> pagesByNumber;
    private final Set<Integer> writtenPages = new HashSet<>();
    private StoryUser reader;
    private Set<StoryUser> listeners;

    public Diary(StoryUser reader, Set<StoryUser> listeners, HashMap<Integer, BaseComponent[]> pagesByNumber,
                 ExItemStack book) {
        this.reader = reader;
        this.listeners = listeners;
        this.pagesByNumber = pagesByNumber;
        this.book = book;
    }

    public Diary(ChapterFile chapterFile, int partId) {
        this.pagesByNumber = new HashMap<>();

        this.book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false).setMoveable(false);

        for (Integer pageNumber : chapterFile.getPathIntegerList(chapterFile.getDiaryPath(partId))) {
            List<String> text = chapterFile.getDiaryText(partId, pageNumber);

            BaseComponent[] components = new BaseComponent[text.size()];

            for (int i = 0; i < text.size(); i++) {
                components[i] = new TextComponent(text.get(i) + "\n");
            }

            this.pagesByNumber.put(pageNumber, components);
        }

        BookMeta meta = ((BookMeta) this.book.getItemMeta());
        meta.spigot().setPages(new BaseComponent[]{new TextComponent()});

        meta.setAuthor("Yourself");
        meta.setTitle("Diary");

        this.book.setItemMeta(meta);
    }

    public Diary clone(StoryUser reader, Set<StoryUser> listeners) {
        return new Diary(reader, listeners, pagesByNumber, this.book.cloneWithId());
    }

    public void loadPage(Integer... pageNumbers) {
        BookMeta meta = (BookMeta) this.book.getItemMeta();
        meta.spigot().setPages();

        this.writtenPages.addAll(Arrays.asList(pageNumbers));

        int pages = this.writtenPages.size() > 0 ? Collections.max(this.writtenPages) : 1;

        for (int page = 1; page <= pages; page++) {
            BaseComponent[] text = this.pagesByNumber.get(page);

            boolean exists = true;

            try {
                meta.getPage(page);
            } catch (IllegalArgumentException e) {
                exists = false;
            }

            if (text != null && (this.writtenPages.contains(page) || !exists)) {
                meta.spigot().addPage(text);
            } else {
                meta.spigot().setPage(page, this.pagesByNumber.get(page));
            }
        }

        if (!meta.hasPages()) {
            meta.spigot().setPages(new BaseComponent[]{});
        }

        meta.setAuthor(this.reader.getName());
        meta.setTitle("Tagebuch");

        this.book.setItemMeta(meta);

        this.reader.setItem(0, this.book);

        this.listeners.forEach(u -> u.setItem(0, this.book));

        if (pageNumbers.length > 0) {
            this.reader.sendActionBarText(Component.text(" mache mir Notizen...", ExTextColor.GOLD));
            this.listeners.forEach(u -> u.sendActionBarText(Component.text("Ich mache mir Notizen...", ExTextColor.GOLD)));
        }
    }

    public ExItemStack getBook() {
        return book;
    }
}
