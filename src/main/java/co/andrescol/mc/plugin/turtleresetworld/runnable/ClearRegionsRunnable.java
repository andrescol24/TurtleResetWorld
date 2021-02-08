package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import co.andrescol.mc.plugin.turtleresetworld.hooks.GriefPreventionClaimer;
import co.andrescol.mc.plugin.turtleresetworld.objects.RegionInFile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ClearRegionsRunnable extends BukkitRunnable {

    private final List<World> worlds;

    /**
     * Creates an instance of this Runnable
     *
     * @param worlds List of worlds that going to be deleted
     */
    public ClearRegionsRunnable(List<World> worlds) {
        this.worlds = worlds;
    }

    @Override
    public void run() {
        List<Claimer> claimers = TurtleResetWorldPlugin.getHooks();

        for (World world : this.worlds) {
            this.resetWorld(world, claimers);
        }

        // Run in the main thread
        PostCleanRegionsRunnable post = new PostCleanRegionsRunnable();
        post.runTask(APlugin.getInstance());
    }

    /**
     * Resets the world
     *
     * @param world    world which will be to reset
     * @param claimers Claimers than protect chunks
     */
    private void resetWorld(World world, List<Claimer> claimers) {
        APlugin plugin = APlugin.getInstance();
        plugin.info("Starting {} regeneration", world.getName());

        // Filter claimed chunks by world
        List<Chunk> protectedChunk = new LinkedList<>();
        for(Claimer claimer : claimers) {
            for(Chunk chunk : claimer.getClaimedChunks()) {
                if(chunk.getWorld().equals(world)) {
                    protectedChunk.add(chunk);
                }
            }
        }

        File worldDirectory = world.getWorldFolder();
        String regionPath = this.getRegionFolder(world);

        File regionFolder = new File(worldDirectory, regionPath);
        if (regionFolder.exists()) {
            File[] files = Objects.requireNonNull(regionFolder.listFiles());
            for (File file : files) {
                this.cleanRegion(file, protectedChunk);
            }
        } else {
            plugin.warn("The region {} file doesn't exist", regionFolder.getAbsolutePath());
        }
    }

    private void cleanRegion(File regionFile, List<Chunk> protectedChunks) {
        APlugin plugin = APlugin.getInstance();
        try {
            RegionInFile region = new RegionInFile(regionFile, protectedChunks);
            if (region.hasClaimedChunks()) {
                plugin.info("The region {} has claimed chunks", region);
                region.removeUnClaimedChunksInFile();
            } else {
                boolean deleted = region.deleteFile();
                if (!deleted) {
                    plugin.warn("The region file {} couldn't be deleted", regionFile.getName());
                } else {
                    plugin.info("Region {} deleted!", region);
                }
            }
        } catch (IllegalStateException e) {
            plugin.error("The region file couldn't be deleted", e);
        } catch (Exception e) {
            plugin.error("The region file {} couldn't be deleted", e, regionFile.getName());
        }
    }

    /**
     * Get the region folder for the world
     *
     * @param world world
     * @return The relative folder path
     */
    private String getRegionFolder(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "DIM-1" + File.separator + "region";
            case THE_END:
                return "DIM1" + File.separator + "region";
            default:
                return "region";
        }
    }
}
