package co.andrescol.mc.plugin.turtleresetworld.runnable.copy;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class UnRegisterEventRunnable extends BukkitRunnable {

    private AntiPlayerJoinListener listener;

    public  UnRegisterEventRunnable(AntiPlayerJoinListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("Allowing players to join again");
        HandlerList.unregisterAll(this.listener);
    }

    @Override
    public String toString() {
        return "RestartServerRunnable";
    }
}
