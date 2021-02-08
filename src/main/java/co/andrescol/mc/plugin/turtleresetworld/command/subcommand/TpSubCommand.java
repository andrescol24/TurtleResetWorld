package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class TpSubCommand extends ASubCommand {
    public TpSubCommand() {
        super("tp", "otro");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Location location = new Location(Bukkit.getWorld(args[1]),
                Double.parseDouble(args[2]),
                Double.parseDouble(args[3]),  Double.parseDouble(args[4]));
        Player player = (Player) commandSender;
        player.teleport(location);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> result = new LinkedList<>();
        Bukkit.getWorlds().forEach(x -> result.add(x.getName()));
        return result;
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String s, String[] strings) {
        return true;
    }
}
