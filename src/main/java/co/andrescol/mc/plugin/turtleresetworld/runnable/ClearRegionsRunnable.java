package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import co.andrescol.mc.plugin.turtleresetworld.hooks.GriefPreventionClaimer;
import co.andrescol.mc.plugin.turtleresetworld.objects.ChunkRegion;
import co.andrescol.mc.plugin.turtleresetworld.objects.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
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
        List<Claimer> claimers = this.chargeHooks();

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

        File worldDirectory = world.getWorldFolder();
        String regionPath = this.getRegionFolder(world);

        File regionFolder = new File(worldDirectory, regionPath);
        if (regionFolder.exists()) {
            File[] files = Objects.requireNonNull(regionFolder.listFiles());
            for (File file : files) {
                this.cleanRegion(file, claimers);
            }
        } else {
            plugin.warn("The region {} file doesn't exist", regionFolder.getAbsolutePath());
        }
    }

    private void cleanRegion(File regionFile, List<Claimer> claimers) {
        APlugin plugin = APlugin.getInstance();
        try {
            Region region = new Region(regionFile);
            if (region.hasClaimedChunks(claimers)) {
                plugin.info("The region {} has claimed chunks", region);
                List<ChunkRegion> removed = region.removeUnClaimedChunks(claimers);
                plugin.info("Chunks removed {}", removed);
                region.saveFile();
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

    /**
     * Charges the hooks
     *
     * @return list of claimers for protect chunks claimed
     */
    private List<Claimer> chargeHooks() {
        List<Claimer> claimers = new LinkedList<>();
        if (Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            APlugin.getInstance().info("There is GriefPrevention plugin!");
            claimers.add(new GriefPreventionClaimer());
        }
        return claimers;
    }
}
