package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * This class only can be executed in the main thread. Use only with @link{{@link BukkitRunnable#runTask(Plugin)}}
 */
public class PostCleanRegionsRunnable extends BukkitRunnable {

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();

        Date date = new Date();
        long timestamp = date.getTime();

        // Save the lastRegenDate
        plugin.reloadConfig();
        plugin.getConfig().set("lastRegenDate", timestamp);
        plugin.saveConfig();

        plugin.info("Restarting server...");
        Bukkit.getServer().spigot().restart();
    }
}
