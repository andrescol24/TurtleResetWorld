package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.configuration.ALanguage;
import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class HookListSubCommand extends ASubCommand {

    public HookListSubCommand() {
        super("hooks", "turtleresetworld.hooks");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String list = TurtleResetWorldPlugin.getHooksList();
        String message = ALanguage.getMessage("HOOKLIST", list);
        AUtils.sendMessage(commandSender, message);
        return true;
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String s, String[] strings) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new LinkedList<>();
    }
}
