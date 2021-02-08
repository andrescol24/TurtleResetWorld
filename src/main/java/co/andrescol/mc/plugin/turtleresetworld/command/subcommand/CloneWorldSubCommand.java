package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

public class CloneWorldSubCommand extends ASubCommand {

    public CloneWorldSubCommand() {
        super("clone", "turtleresetworld.clone");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        // Create world
        WorldCreator creator = new WorldCreator("cloneOf_" + worldName)
                .environment(world.getEnvironment()).seed(world.getSeed());
        World clone = creator.createWorld();

        List<Claimer> claimers = TurtleResetWorldPlugin.getHooks();
        for(Claimer claimer : claimers) {
            for(Chunk chunk : claimer.getClaimedChunks()) {
                if(chunk.getWorld().equals(world)) {
                    clone.loadChunk(chunk.getX(), chunk.getZ());
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                APlugin.getInstance().info("Copying blocks...");
                for(Claimer claimer : claimers) {
                    for(Chunk chunk : claimer.getClaimedChunks()) {
                        if(chunk.getWorld().equals(world)) {
                            Chunk newChunk = clone.getChunkAt(chunk.getX(), chunk.getZ());
                            APlugin.getInstance().info("Copying blocks for chunk {}", chunk);
                            for(int y = 0; y < 255; y++) {
                                for(int x = 0; x < 16; x++) {
                                    for(int z = 0; z < 16; z++) {
                                        Block block = chunk.getBlock(x, y, z);
                                        newChunk.getBlock(x, y, z).setBlockData(block.getBlockData());
                                    }
                                }
                            }
                        }
                    }
                }
                APlugin.getInstance().info("Copying blocks finished");
            }
        }.runTask(APlugin.getInstance());

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
