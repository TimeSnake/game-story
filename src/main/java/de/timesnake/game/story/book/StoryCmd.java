package de.timesnake.game.story.book;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.story.user.StoryUser;
import de.timesnake.library.basic.util.cmd.Arguments;
import de.timesnake.library.basic.util.cmd.ExCommand;

import java.util.HashSet;
import java.util.List;

public class StoryCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!sender.isPlayer(true)) {
            return;
        }

        StoryUser user = ((StoryUser) sender.getUser());

        if (!args.isLengthEquals(2, true)) {
            return;
        }

        if (!args.get(0).isInt(true) || !args.get(1).isInt(true)) {
            return;
        }

        user.startChapterPart(args.get(0).toInt(), args.get(1).toInt(), new HashSet<>());


    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
