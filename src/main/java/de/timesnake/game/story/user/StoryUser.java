package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.permission.Group;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.story.DbStoryUser;
import de.timesnake.game.story.book.StoryContentBook;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.game.story.structure.StoryPart;
import de.timesnake.game.story.structure.StorySection;
import de.timesnake.library.basic.util.chat.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;

public class StoryUser extends User {


    private final Map<Integer, Set<Integer>> boughtPartsByChapter = new HashMap<>();
    private final Map<Integer, Map<Integer, Integer>> sectionsByPartByChapter = new HashMap<>();

    private final DbStoryUser dbStory;
    private final StoryContentBook contentBook;
    private StoryChapter chapter;
    private StoryPart part;
    private StorySection section;
    private ExWorld world;

    public StoryUser(Player player) {
        super(player);

        this.world = Server.getWorld(this.getUniqueId().toString());

        if (this.world == null) {
            this.world = Server.getWorldManager().cloneWorld(this.getUniqueId().toString(),
                    StoryServer.getStoryWorldTemplate());
        }

        LinkedList<TablistGroupType> types = new LinkedList<>();
        types.add(Group.getTablistType());
        Tablist tablist = Server.getScoreboardManager().registerNewGroupTablist(this.getName(), Tablist.Type.DUMMY,
                types, (e, t) -> {
                }, (e, t) -> {
                });

        tablist.setHeader("§6Time§2Snake§9.de");
        tablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \n" + Server.SUPPORT_EMAIL);

        tablist.addEntry(this);

        this.setTablist(tablist);

        this.world.setPVP(false);
        this.world.allowBlockPlace(false);
        this.world.allowBlockBreak(false);
        this.world.allowFluidCollect(false);
        this.world.allowFluidPlace(false);
        this.world.allowBlockBurnUp(false);
        this.world.allowBlockIgnite(false);
        this.world.allowFlintAndSteel(false);
        this.world.allowLightUpInteraction(true);
        this.world.allowFireSpread(false);
        this.world.allowEntityExplode(false);
        this.world.allowEntityBlockBreak(false);
        this.world.allowItemFrameRotate(true);
        this.world.setExceptService(true);
        this.world.allowDropPickItem(true);
        this.world.allowPlaceInBlock(true);
        this.world.allowCakeEat(false);
        this.world.setLockedBlockInventories(List.of(Material.DISPENSER, Material.DROPPER, Material.HOPPER));
        this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);

        this.dbStory = Database.getStory().getUser(this.getUniqueId());

        for (Integer chapterId : StoryServer.getChapters().stream().map(StoryChapter::getId).toList()) {
            this.boughtPartsByChapter.put(chapterId, this.dbStory.getBoughtParts(chapterId));
        }

        for (Integer chapterId : dbStory.getChapterIds()) {

            HashMap<Integer, Integer> sectionIdsByPartId = new HashMap<>();

            for (Integer partId : dbStory.getPartIds(chapterId)) {
                Integer sectionId = dbStory.getSectionId(chapterId, partId);

                sectionIdsByPartId.put(partId, sectionId);
            }

            this.sectionsByPartByChapter.put(chapterId, sectionIdsByPartId);
        }

        if (this.sectionsByPartByChapter.isEmpty()) {
            HashMap<Integer, Integer> sectionsByPart = new HashMap<>();
            sectionsByPart.put(1, 1);
            this.sectionsByPartByChapter.put(1, sectionsByPart);
        }

        this.contentBook = new StoryContentBook(this.sectionsByPartByChapter, this.boughtPartsByChapter);
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

        Integer sectionId = this.sectionsByPartByChapter.get(chapterId).get(partId);

        if (sectionId > chapter.getPart(partId).getLastSection().getId()) {
            this.sendPluginMessage(Plugin.STORY, ChatColor.WARNING + "You already played this part");
            return;
        }

        this.part = chapter.getPart(partId).clone(this, listeners);

        this.section = this.part.getSection(sectionId);

        this.dbStory.setSectionId(this.chapter.getId(), this.part.getId(), this.section.getId());

        this.clearInventory();
        this.setGameMode(GameMode.SURVIVAL);

        this.setItem(0, this.part.getDiary().getBook());
        this.setItem(1, UserManager.FOOD);
        this.setItem(2, UserManager.DRINK);
        this.setItem(8, UserManager.CHECKPOINT);

        this.section.start(true, true);
    }

    public void onCompletedSection(StorySection section, Set<StoryUser> listeners) {

        section.stop();

        this.section = this.part.nextSection(section);

        Server.runTaskLaterSynchrony(() -> {
            if (this.section == null) {
                this.sectionsByPartByChapter.get(this.chapter.getId()).put(this.part.getId(), section.getId() + 1);
                this.dbStory.setSectionId(this.chapter.getId(), this.part.getId(), section.getId() + 1);
                Server.printText(Plugin.STORY, "Completed part " + this.chapter.getId() + "." + this.part.getId(),
                        this.getName());
                this.onCompletedPart(this.part);
            } else {
                this.sectionsByPartByChapter.get(this.chapter.getId()).put(this.part.getId(), this.section.getId());
                this.dbStory.setSectionId(this.chapter.getId(), this.part.getId(), this.section.getId());
                Server.printText(Plugin.STORY,
                        "Saved checkpoint " + this.chapter.getId() + "." + this.part.getId() + "." + this.section.getId(), this.getName());
                this.sendPluginMessage(Plugin.STORY, ChatColor.PERSONAL + "Checkpoint");

                this.section.start(false, true);
            }
        }, 5 * 20, GameStory.getPlugin());

    }

    public void onCompletedPart(StoryPart part) {
        this.sendTitle("", part.getEndMessage(), Duration.ofSeconds(3));

        this.sectionsByPartByChapter.get(this.chapter.getId()).put(part.getId() + 1, 1);

        Server.runTaskLaterSynchrony(() -> {
            this.joinStoryHub();
            part.despawnCharacters();
        }, 5 * 20, GameStory.getPlugin());
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

    public Set<Integer> getBoughtParts(Integer chapterId) {
        return this.boughtPartsByChapter.get(chapterId);
    }

    public void buyPart(Integer chapterId, Integer partId) {
        this.removeCoins(StoryServer.PART_PRICE, true);
        this.boughtPartsByChapter.get(chapterId).add(partId);
        this.dbStory.addBoughtPart(chapterId, partId);
    }

    public ExLocation getStoryRespawnLocation() {
        return this.section.getStartLocation();
    }

    public StoryChapter getChapter() {
        return chapter;
    }

    public StoryPart getPart() {
        return part;
    }

    public StorySection getSection() {
        return section;
    }
}
