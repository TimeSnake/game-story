package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class Diary {

    private static final String PAGES = "pages";

    private final ExItemStack book = new ExItemStack(Material.WRITTEN_BOOK).setDropable(false);
    private int pageCounter = 1;

    private final ExFile file;

    public Diary(ExFile diaryFile) {
        this.file = diaryFile;

        this.loadFromFile();
    }

    private void loadFromFile() {

        List<BaseComponent[]> pages = new ArrayList<>();

        pages.add(new BaseComponent[]{new TextComponent("§lYour Diary")});

        for (Integer pageNumber : this.file.getPathIntegerList(PAGES)) {
            List<BaseComponent> components = new ArrayList<>();

            for (String component : this.file.getStringList(PAGES + "." + pageNumber)) {
                components.add(new TextComponent(component));
            }

            pages.add(components.toArray(new BaseComponent[0]));

            this.pageCounter++;
        }

        BookMeta meta = ((BookMeta) this.book.getItemMeta());
        meta.spigot().setPages(pages);

        if (!meta.hasPages()) {
            meta.spigot().setPages(new BaseComponent[]{});
        }

        meta.setAuthor("Yourself");
        meta.setTitle("Diary");

        this.book.setItemMeta(meta);
    }

    public void saveToFile() {
        file.remove(PAGES);

        int i = 1;
        for (BaseComponent[] page : ((BookMeta) book.getItemMeta()).spigot().getPages()) {
            List<String> components = new ArrayList<>();
            for (BaseComponent component : page) {
                components.add(component.toLegacyText());
            }

            this.file.set(PAGES + "." + i, components);

            i++;
        }
    }

    public int addPage(BaseComponent[] components) {
        BookMeta meta = (BookMeta) this.book.getItemMeta();

        meta.spigot().addPage(components);

        this.book.setItemMeta(meta);
        return this.pageCounter++;
    }

    public ExItemStack getBook() {
        return book;
    }
}
