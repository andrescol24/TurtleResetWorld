package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TpsSubCommand extends ASubCommand {

    public TpsSubCommand() {
        super("tps", "turtleresetworld.tps");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        List<World> worlds = Bukkit.getWorlds();
        if(!worlds.isEmpty()) {
            World world = worlds.get(0);
            world.getChunkAt(0, 0);
        }
        return true;
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String s, String[] strings) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
