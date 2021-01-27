package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;

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
        for (World world : this.worlds) {
            this.resetWorld(world);
        }

        // Run in the main thread
        PostCleanRegionsRunnable post = new PostCleanRegionsRunnable();
        post.runTask(APlugin.getInstance());
    }

    /**
     * Resets the world
     *
     * @param world world which will be to reset
     */
    private void resetWorld(World world) {
        APlugin plugin = APlugin.getInstance();
        plugin.info("Starting {} regeneration...", world.getName());

        File worldDirectory = world.getWorldFolder();
        String regionPath = this.getRegionFolder(world);

        File regionFolder = new File(worldDirectory, regionPath);
        if (regionFolder.exists()) {
            for (File file : regionFolder.listFiles()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    plugin.warn("The region file {} for world {} couldn't be deleted", file.getName(), world.getName());
                }
            }
        } else {
            plugin.warn("The region {} file doesn't exist", regionFolder.getAbsolutePath());
        }
    }

    /**
     * Get the region folder for the world
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
