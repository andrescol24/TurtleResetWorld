package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.ReadingDataException;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class RestartServerRunnable extends BukkitRunnable {

    private final boolean regenerationSuccess;

    public RestartServerRunnable(boolean regenerationSuccess) {
        this.regenerationSuccess = regenerationSuccess;
    }

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();

        // Save the lastRegenDate
        this.saveRegeneration();

        plugin.info("Restarting server...");
        Bukkit.getServer().spigot().restart();
    }

    private void saveRegeneration() {
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        try {
            var data = dataManager.getData();
            Date date = new Date();
            long timestamp = date.getTime();

            data.setLastRegeneration(timestamp);
            data.setLastRegenerationSuccessFully(this.regenerationSuccess);
            dataManager.saveData();
        } catch (ReadingDataException e) {
            APlugin.getInstance().error("Error reading the regeneration data before to restarting", e);
        }
    }

    @Override
    public String toString() {
        return String.format("RestartServerRunnable");
    }
}
