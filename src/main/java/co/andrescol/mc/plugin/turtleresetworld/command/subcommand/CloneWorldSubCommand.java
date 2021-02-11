package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import co.andrescol.mc.plugin.turtleresetworld.runnable.OrchestratorRegenRunnable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

public class CloneWorldSubCommand extends ASubCommand {

    public CloneWorldSubCommand() {
        super("clone", "turtleresetworld.clone");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
//        OrchestratorRegenRunnable orchestrator = new OrchestratorRegenRunnable();
//        orchestrator.runTaskAsynchronously(APlugin.getInstance());
  //      APlugin.getInstance().info("Finished in onCommand!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = new LinkedList<>();
        if (args.length <= 2) {
            String name = args.length == 2 ? args[1] : "";
            List<World> worlds = Bukkit.getWorlds();
            worlds.forEach(x -> {
                if (x.getName().startsWith(name)) {
                    list.add(x.getName());
                }
            });
        }
        return list;
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 2) {
            World world = Bukkit.getWorld(args[1]);
            return world != null;
        }
        return false;
    }
}
