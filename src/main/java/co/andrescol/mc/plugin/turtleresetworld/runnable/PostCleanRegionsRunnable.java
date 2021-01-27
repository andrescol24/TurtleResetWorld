package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class only can be executed in the main thread. Use only with @link{{@link BukkitRunnable#runTask(Plugin)}}
 */
public class PostCleanRegionsRunnable extends BukkitRunnable {

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("The regeneration finished. Restarting");
        Bukkit.getServer().spigot().restart();
    }
}
