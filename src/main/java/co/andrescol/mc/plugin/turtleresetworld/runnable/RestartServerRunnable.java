package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class RestartServerRunnable extends BukkitRunnable {

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
