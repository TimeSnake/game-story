package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.story.DbStoryUser;
import de.timesnake.game.story.book.Diary;
import de.timesnake.game.story.book.StoryContentBook;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.structure.StoryPart;
import de.timesnake.game.story.structure.StorySection;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StoryUser extends User {

    private final Map<Integer, Map<Integer, Integer>> sectionsByPartByChapter = new HashMap<>();

    private StoryChapter chapter;
    private StoryPart part;
    private StorySection section;

    private Diary diary;

    private final StoryContentBook contentBook;

    private ExWorld world;

    public StoryUser(Player player) {
        super(player);

        this.world = Server.getWorld(this.getUniqueId().toString());

        DbStoryUser dbStory = Database.getStory().getUser(this.getUniqueId());

        for (Integer chapterId : dbStory.getChapterIds()) {

            HashMap<Integer, Integer> sectionIdsByPartId = new HashMap<>();

            for (Integer partId : dbStory.getPartIds(chapterId)) {
                Integer sectionId = dbStory.getSectionId(chapterId, partId);

                sectionIdsByPartId.put(partId, sectionId);
            }

            this.sectionsByPartByChapter.put(chapterId, sectionIdsByPartId);
        }

        this.contentBook = new StoryContentBook(this.sectionsByPartByChapter);
    }

    public void startChapterPart(Integer chapterId, Integer partId, Set<StoryUser> listeners) {
        StoryChapter chapter = StoryServer.getChapter(chapterId);

        if (chapter == null) {
            return;
        }

        StoryPart part = chapter.getPart(partId);

        if (part == null) {
            return;
        }

        this.chapter = chapter;

        this.sectionsByPartByChapter.get(chapterId).putIfAbsent(partId, 1);

        this.part = chapter.getPart(partId).clone(this, listeners);

        Integer sectionId = this.sectionsByPartByChapter.get(chapterId).get(partId);

        this.section = this.part.getSection(sectionId);

        this.prepareGame();

        this.section.start();
    }

    private void prepareGame() {
        this.diary = StoryServer.getDiaryManager().getUserDiary(this.getUniqueId(), this.chapter.getId());

        this.clearInventory();

        this.setItem(0, this.diary.getBook());
        this.setItem(1, UserManager.FOOD);
    }

    public void onCompletedSection(StorySection section, Set<StoryUser> listeners) {
        this.section = this.part.nextSection(section);

        section.stop();

        if (this.section == null) {
            this.onCompletedPart(this.part);
            return;
        }

        this.section.start();
    }

    public void onCompletedPart(StoryPart part) {
        this.sendTitle("", part.getEndMessage(), Duration.ofSeconds(3));
        Server.runTaskLaterSynchrony(this::joinStoryHub, 5 * 20, GameStory.getPlugin());
    }

    public void stopStory() {
        if (this.section != null) {
            this.section.clearEntities();
        }
    }

    public void joinStoryHub() {
        this.setDefault();
        this.setItem(0, this.contentBook.getBook());
        this.teleport(StoryServer.getBaseWorld());
    }

    public ExWorld getStoryWorld() {
        return this.world;
    }

    public StoryContentBook getContentBook() {
        return contentBook;
    }

    public Diary getDiary() {
        return diary;
    }

    public void updateDiary() {
        this.setItem(0, this.diary.getBook());
    }
}
