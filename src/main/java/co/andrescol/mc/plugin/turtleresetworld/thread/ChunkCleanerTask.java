package co.andrescol.mc.plugin.turtleresetworld.thread;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.listener.PlayerJoinListener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkCleanerTask extends BukkitRunnable {

    private final PlayerJoinListener listener;

    /**
     * Creates an instance of this Runnable
     * @param listener listener which is going to be unregister
     */
    public ChunkCleanerTask(PlayerJoinListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        for(int i = 0; i < 10; i++) {
            APlugin.getInstance().info("Second #{}", i);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        APlugin.getInstance().info("Cleaning is over. It's Allowing players to join");
        PlayerJoinEvent.getHandlerList().unregister(listener);
    }
}
