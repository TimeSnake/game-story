/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.story.user;

import de.timesnake.basic.bukkit.core.user.UserPlayerDelegation;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractListener;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.game.story.chat.Plugin;
import de.timesnake.game.story.element.TalkType;
import de.timesnake.game.story.main.GameStory;
import de.timesnake.game.story.server.StoryServer;
import de.timesnake.game.story.structure.Difficulty;
import de.timesnake.game.story.structure.Quest;
import de.timesnake.game.story.structure.StoryBook;
import de.timesnake.game.story.structure.StoryChapter;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.player.UserList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class StoryReader implements Iterable<StoryUser> {

    private final UUID id = UUID.randomUUID();

    private final StoryUser host;
    private final UserList<StoryUser> users;

    private final StartInventory startInventory;

    private TalkType talkType = TalkType.TEXT;
    private Difficulty difficulty = Difficulty.NORMAL;

    private StoryBook book;
    private StoryChapter chapter;
    private Quest quest;
    private int deaths = 0;

    private boolean performedPreChecks = false;

    public StoryReader(StoryUser host, Collection<StoryUser> users, int bookId, String chapterName) {
        this.host = host;
        this.users = new UserList<>(users);

        this.book = StoryServer.getBook(bookId);
        this.chapter = this.book.getChapter(chapterName).clone(this);

        this.startInventory = new StartInventory();
        this.host.openInventory(this.startInventory.getInventory());
        this.forEach(User::clearInventory);
    }

    public UUID getId() {
        return id;
    }

    public TalkType getTalkType() {
        return talkType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean matchAnyUser(Predicate<StoryUser> predicate) {
        return this.users.stream().anyMatch(predicate);
    }

    public List<StoryUser> filterUsers(Predicate<StoryUser> predicate) {
        return this.users.stream().filter(predicate).toList();
    }

    public List<StoryUser> getUsers() {
        return this.users;
    }

    public boolean containsUser(StoryUser user) {
        return this.users.contains(user);
    }

    public StoryUser anyUser() {
        return this.users.stream().findAny().get();
    }

    public void removeUser(StoryUser user) {
        this.users.remove(user);
        user.setReaderGroup(null);
        Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.STORY_END,
                Server.getChannel().getHost().getPort()));

        if (this.getUsers().isEmpty()) {
            this.destroy();
        } else {
            this.saveProgress();
        }
    }

    public void clearUsers() {
        for (StoryUser user : this) {
            user.setReaderGroup(null);
            Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.STORY_END,
                    Server.getChannel().getHost().getPort()));
        }

        this.users.clear();
    }

    public ExWorld getWorld() {
        if (this.chapter != null) {
            return this.chapter.getWorld();
        }
        return null;
    }

    public void onCompletedQuest(Quest quest) {
        quest.end();
        Server.printText(Plugin.STORY, Chat.listToString(this.users.stream()
                .map(UserPlayerDelegation::getName).toList()) + " completed '" + quest.getName() + "'");

        if (!quest.equals(this.quest)) {
            quest.nextQuest();
            return;
        }

        Quest next = this.quest.nextQuest();
        if (next == null) {
            this.onCompletedChapter();
            return;
        }

        this.quest = next;

        this.saveProgress();

        this.quest.start(false, true);
    }

    private void onCompletedChapter() {
        this.forEach(u -> u.showTitle(Component.empty(), Component.text(this.chapter.getEndMessage()), Duration.ofSeconds(3)));

        // TODO save progress

        Server.runTaskLaterSynchrony(() -> {
            this.forEach(StoryUser::joinStoryHub);
            this.destroy();
        }, 5 * 20, GameStory.getPlugin());
    }

    public void setSelectedQuest(String name) {
        this.quest.setSelectedQuest(name);
    }

    @NotNull
    @Override
    public Iterator<StoryUser> iterator() {
        return this.users.iterator();
    }

    public void startReading() {
        if (!this.performedPreChecks) {
            if (this.runPreChecks()) {
                return;
            }
        }

        String savedQuestName = this.host.getProgress().getQuest(this.book.getId(), this.chapter.getName());
        if (savedQuestName != null) {
            this.quest = this.chapter.getQuest(savedQuestName);
        } else {
            this.quest = this.chapter.getFirstQuest();
        }

        // TODO update progress

        this.quest = this.chapter.getFirstQuest();

        // TODO update progress

        ExItemStack diary = this.chapter.getDiary().getBook();

        this.forEach(u -> {
            u.clearInventory();
            u.setGameMode(GameMode.SURVIVAL);

            u.setItem(0, diary);
            u.setItem(1, UserManager.FOOD);
            u.setItem(2, UserManager.DRINK);
            //u.setItem(8, UserManager.CHECKPOINT);

            u.setPlaying(true);
        });

        if (this.difficulty == Difficulty.EASY) {
            this.chapter.getWorld().setDifficulty(org.bukkit.Difficulty.EASY);
        } else if (this.difficulty == Difficulty.NORMAL) {
            this.chapter.getWorld().setDifficulty(org.bukkit.Difficulty.NORMAL);
        } else if (this.difficulty == Difficulty.HARD) {
            this.chapter.getWorld().setDifficulty(org.bukkit.Difficulty.HARD);
        }

        this.chapter.spawnCharacters();
        this.quest.start(true, true);
    }

    private boolean runPreChecks() {
        boolean checksPerformed = false;

        if (chapter.getMaxDeaths(this.difficulty) != null) {
            this.forEach(u -> u.sendPluginMessage(Plugin.STORY, Component.text("Max. Respawns: " +
                    chapter.getMaxDeaths(this.difficulty), ExTextColor.WARNING)));
        }

        if (this.talkType == TalkType.AUDIO) {
            this.forEach(u -> u.sendPluginMessage(Plugin.STORY,
                    Component.text("Login to our website and start the audio check now: ", ExTextColor.PERSONAL)
                            .append(Component.text("https://timesnake.de/story", ExTextColor.VALUE, TextDecoration.UNDERLINED)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy")))
                                    .clickEvent(ClickEvent.openUrl("https://timesnake.de/story/interface/?story=" + book.getId())))));
            this.forEach(u -> Server.getChannel().sendMessage(new ChannelUserMessage<>(u.getUniqueId(),
                    MessageType.User.STORY_START, Server.getChannel().getHost().getPort())));
            checksPerformed = true;
        }

        this.host.setItem(8, this.startInventory.start);
        this.host.sendPluginMessage(Plugin.STORY, Component.text("Click on the start item if you are ready",
                ExTextColor.WARNING));

        this.performedPreChecks = true;
        return checksPerformed;
    }

    public void destroy() {
        this.saveProgress();

        this.clearUsers();

        if (this.quest != null) {
            this.quest.clearEntities();
        }

        this.forEach(u -> u.setReaderGroup(null));
        Server.getWorldManager().deleteWorld(this.getWorld(), true);

        this.book = null;
        this.chapter = null;
        this.quest = null;
        this.deaths = 0;

    }

    public void saveProgress() {
        // TODO save progress from current quest
    }

    public StoryBook getBook() {
        return book;
    }

    public StoryChapter getChapter() {
        return chapter;
    }

    public Quest getQuest() {
        return quest;
    }

    public void addDeath() {
        if (this.chapter.getMaxDeaths(this.difficulty) == null) {
            return;
        }

        this.deaths++;

        int respawnsLeft = this.chapter.getMaxDeaths(this.difficulty) - this.deaths;

        if (respawnsLeft >= 0) {
            this.forEach(u -> u.showTitle(Component.empty(),
                    Component.text(respawnsLeft + " respawns left", ExTextColor.WARNING),
                    Duration.ofSeconds(3)));
        } else {
            this.forEach(u -> u.showTitle(Component.empty(),
                    Component.text("Game Over", ExTextColor.WARNING),
                    Duration.ofSeconds(3)));

            Server.runTaskLaterSynchrony(() -> {
                this.forEach(StoryUser::joinStoryHub);
                this.chapter.despawnCharacters();
                this.destroy();
            }, 3 * 20, GameStory.getPlugin());
        }
    }

    public class StartInventory implements InventoryHolder, UserInventoryClickListener, UserInventoryInteractListener {

        private static final ExItemStack TALK_TYPE = new ExItemStack(Material.COMPASS).setDisplayName("§9Talk Type")
                .setSlot(9).setMoveable(false).setDropable(false).immutable();
        private static final ExItemStack DIFFICULTY = new ExItemStack(Material.COMPASS).setDisplayName("§9Difficulty")
                .setSlot(27).setMoveable(false).setDropable(false).immutable();

        private final ExItemStack talkTypeText = new ExItemStack(Material.PAPER).setDisplayName("§fText")
                .setSlot(10).setMoveable(false).setDropable(false).enchant();
        private final ExItemStack talkTypeAudio = new ExItemStack(Material.GOAT_HORN).setDisplayName("§fAudio")
                .setSlot(11).setMoveable(false).setDropable(false);

        private final ExItemStack difficultyEasy = new ExItemStack(Material.GREEN_DYE).setDisplayName("§fEasy")
                .setSlot(28).setMoveable(false).setDropable(false);
        private final ExItemStack difficultyNormal = new ExItemStack(Material.YELLOW_DYE).setDisplayName("§fNormal")
                .setSlot(29).setMoveable(false).setDropable(false).enchant();
        private final ExItemStack difficultyHard = new ExItemStack(Material.RED_DYE).setDisplayName("§fHard")
                .setSlot(30).setMoveable(false).setDropable(false);

        private final ExItemStack start = new ExItemStack(Material.CLOCK).setDisplayName("§cStart")
                .setSlot(53).setMoveable(false).setDropable(false);

        private final ExInventory inventory;

        public StartInventory() {
            this.inventory = new ExInventory(6 * 9, Component.text("Start Menu"), this);
            this.inventory.setItemStack(TALK_TYPE);
            this.inventory.setItemStack(DIFFICULTY);
            this.inventory.setItemStack(talkTypeText);
            this.inventory.setItemStack(talkTypeAudio);
            this.inventory.setItemStack(difficultyEasy);
            this.inventory.setItemStack(difficultyNormal);
            this.inventory.setItemStack(difficultyHard);
            this.inventory.setItemStack(start);

            Server.getInventoryEventManager().addClickListener(this, this);
            Server.getInventoryEventManager().addInteractListener(this, this.start);
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory.getInventory();
        }

        @Override
        public void onUserInventoryClick(UserInventoryClickEvent event) {
            ExItemStack item = event.getClickedItem();

            if (item.equals(this.talkTypeText)) {
                StoryReader.this.talkType = TalkType.TEXT;
                this.talkTypeAudio.disenchant();
                this.talkTypeText.enchant();
            } else if (item.equals(this.talkTypeAudio)) {
                StoryReader.this.talkType = TalkType.AUDIO;
                this.talkTypeText.disenchant();
                this.talkTypeAudio.enchant();
            } else if (item.equals(this.difficultyEasy)) {
                StoryReader.this.difficulty = Difficulty.EASY;
                this.difficultyEasy.enchant();
                this.difficultyNormal.disenchant();
                this.difficultyHard.disenchant();
            } else if (item.equals(this.difficultyNormal)) {
                StoryReader.this.difficulty = Difficulty.NORMAL;
                this.difficultyNormal.enchant();
                this.difficultyEasy.disenchant();
                this.difficultyHard.disenchant();
            } else if (item.equals(this.difficultyHard)) {
                StoryReader.this.difficulty = Difficulty.HARD;
                this.difficultyHard.enchant();
                this.difficultyEasy.disenchant();
                this.difficultyNormal.disenchant();
            } else if (item.equals(this.start)) {
                event.getUser().closeInventory();
                StoryReader.this.runPreChecks();
            }

            this.inventory.setItemStack(this.talkTypeText);
            this.inventory.setItemStack(this.talkTypeAudio);
            this.inventory.setItemStack(this.difficultyEasy);
            this.inventory.setItemStack(this.difficultyNormal);
            this.inventory.setItemStack(this.difficultyHard);
            this.inventory.setItemStack(this.start);

            this.inventory.update();
        }

        @Override
        public void onUserInventoryInteract(UserInventoryInteractEvent event) {
            ExItemStack item = event.getClickedItem();

            if (item.equals(this.start)) {
                StoryReader.this.startReading();
            }
        }
    }
}
