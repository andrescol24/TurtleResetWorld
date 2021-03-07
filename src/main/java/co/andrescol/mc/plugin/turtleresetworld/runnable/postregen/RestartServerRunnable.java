package co.andrescol.mc.plugin.turtleresetworld.runnable.postregen;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartServerRunnable extends BukkitRunnable {

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("Restarting server...");
        Bukkit.getServer().spigot().restart();
    }

    @Override
    public String toString() {
        return "RestartServerRunnable";
    }
}
