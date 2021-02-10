package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
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
        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        // Create world
        new BukkitRunnable() {
            @Override
            public void run() {
                WorldCreator creator = new WorldCreator("cloneOf_" + worldName)
                        .environment(world.getEnvironment()).seed(world.getSeed());
                World clone = creator.createWorld();

                List<Chunk> chunksClaimed = TurtleResetWorldPlugin.getChunksClaimedByHooks();

                APlugin.getInstance().info("Loading chunks...");
                for (Chunk chunk : chunksClaimed) {
                    if (chunk.getWorld().equals(world)) {
                        clone.loadChunk(chunk.getX(), chunk.getZ());
                    }
                }

                APlugin.getInstance().info("Copying blocks...");
                for (Chunk chunk : chunksClaimed) {
                    if (chunk.getWorld().equals(world)) {
                        Chunk newChunk = clone.getChunkAt(chunk.getX(), chunk.getZ());
                        APlugin.getInstance().info("Copying blocks for chunk {}", chunk);
                        for (int y = 0; y < 255; y++) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    Block block = chunk.getBlock(x, y, z);

                                    // Coping block data
                                    BlockData data = block.getBlockData();
                                    Block newBlock = newChunk.getBlock(x, y, z);
                                    newBlock.setBlockData(data);

                                    // Coping block state
                                    BlockState state = block.getState();
                                    newBlock.getState().setData(state.getData());

                                    if (state instanceof Container) {
                                        Container newContainer = (Container) newBlock.getState();
                                        Container oldContainer = (Container) state;
                                        newContainer.getInventory().setContents(oldContainer.getInventory().getContents());
                                        newContainer.setCustomName(oldContainer.getCustomName());
                                    }

                                    if (state instanceof Nameable) {
                                        Nameable newNameable = (Nameable) newBlock.getState();
                                        Nameable oldNameable = (Nameable) state;
                                        newNameable.setCustomName(oldNameable.getCustomName());
                                    }
                                }
                            }
                        }
                        APlugin.getInstance().info("Moving entities");
                        for (Entity entity : chunk.getEntities()) {
                            Location entityLocation = entity.getLocation();
                            entityLocation.setWorld(clone);
                            entity.teleport(entityLocation);
                        }
                    }
                }
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
