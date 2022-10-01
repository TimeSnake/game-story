package de.timesnake.game.story.book;

import com.moandjiezana.toml.Toml;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.story.user.StoryReader;
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
    private StoryReader reader;

    public Diary(StoryReader reader, HashMap<Integer, BaseComponent[]> pagesByNumber,
                 ExItemStack book) {
        this.reader = reader;
        this.pagesByNumber = pagesByNumber;
        this.book = book;
    }

    public Diary(Toml toml, int chapterId) {
        this.pagesByNumber = new HashMap<>();

        this.book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false).setMoveable(false);

        for (Map.Entry<String, Object> entry : toml.entrySet()) {
            int pageNumber = Integer.parseInt(entry.getKey());
            List<String> text = (List<String>) entry.getValue();

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

    public Diary clone(StoryReader reader) {
        return new Diary(reader, pagesByNumber, this.book.cloneWithId());
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
