package co.andrescol.mc.plugin.turtleresetworld.thread;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.listener.PlayerJoinListener;
import org.bukkit.World;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;

public class ResetWorldThread extends BukkitRunnable {

    private final PlayerJoinListener listener;
    private final List<World> worlds;

    /**
     * Creates an instance of this Runnable
     *
     * @param listener listener which is going to be unregister
     */
    public ResetWorldThread(PlayerJoinListener listener, List<World> worlds) {
        this.listener = listener;
        this.worlds = worlds;
    }

    @Override
    public void run() {
        for (World world : this.worlds) {
            this.resetWorld(world);
        }
        APlugin.getInstance().info("Cleaning is over. It's Allowing players to join");
        PlayerJoinEvent.getHandlerList().unregister(listener);
    }

    /**
     * Resets the world
     *
     * @param world world which will be to reset
     */
    private void resetWorld(World world) {
        APlugin.getInstance().info("Starting {} regeneration...", world);
        File worldDirectory = world.getWorldFolder();
        String regionPath;
        switch (world.getEnvironment()) {
            case NETHER:
                regionPath = "DIM-1" + File.separator + "region";
                break;
            case THE_END:
                regionPath = "DIM1" + File.separator + "region";
                break;
            default:
                regionPath = "region";
        }
        File regionFolder = new File(worldDirectory, regionPath);
        if (regionFolder.exists()) {
            for (File file : regionFolder.listFiles()) {
                APlugin.getInstance().info("Deleting {}",
                        file.getName());
            }
        } else {
            APlugin.getInstance().warn("The region {} file doesn't exist", regionFolder.getAbsolutePath());
        }
    }
}
