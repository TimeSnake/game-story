/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.book;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.story.user.StoryReader;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class Diary {

    private final ExItemStack book;

    private final HashMap<Integer, Component> pagesByNumber;
    private final Set<Integer> writtenPages = new HashSet<>();
    private StoryReader reader;

    public Diary(StoryReader reader, HashMap<Integer, Component> pagesByNumber,
                 ExItemStack book) {
        this.reader = reader;
        this.pagesByNumber = pagesByNumber;
        this.book = book;
    }

    public Diary(Toml toml) {
        this.pagesByNumber = new HashMap<>();

        this.book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false).setMoveable(false);

        if (toml != null) {
            for (Map.Entry<String, Object> entry : toml.entrySet()) {
                int pageNumber = Integer.parseInt(entry.getKey());
                List<String> text = (List<String>) entry.getValue();

                Component component = Component.empty();

                for (String s : text) {
                    component = component.append(Component.text(s)
                            .append(Component.newline()));
                }

                this.pagesByNumber.put(pageNumber, component);
            }
        }

        BookMeta meta = ((BookMeta) this.book.getItemMeta());
        meta.addPages(Component.empty());

        meta.setAuthor("Yourself");
        meta.setTitle("Diary");

        this.book.setItemMeta(meta);
    }

    public Diary clone(StoryReader reader) {
        return new Diary(reader, pagesByNumber, this.book.cloneWithId());
    }

    public void loadPage(Integer... pageNumbers) {
        BookMeta meta = (BookMeta) this.book.getItemMeta();

        this.writtenPages.addAll(Arrays.asList(pageNumbers));

        int pages = this.writtenPages.size() > 0 ? Collections.max(this.writtenPages) : 1;

        for (int page = 1; page <= pages; page++) {
            Component text = this.pagesByNumber.get(page);
            if (meta.getPageCount() < page) {
                meta.addPages(text);
            } else {
                meta.page(page, this.pagesByNumber.get(page));
            }
        }

        if (!meta.hasPages()) {
            meta.addPages(Component.empty());
        }

        meta.setAuthor("Yourself");
        meta.setTitle("Tagebuch");

        this.book.setItemMeta(meta);

        this.reader.forEach(u -> u.setItem(0, this.book));

        if (pageNumbers.length > 0) {
            this.reader.forEach(u -> u.sendActionBarText(Component.text("Ich mache mir Notizen...", ExTextColor.GOLD)));
        }
    }

    public ExItemStack getBook() {
        return book;
    }
}
