package co.andrescol.mc.plugin.turtleresetworld.listener;

import co.andrescol.mc.library.configuration.ALanguage;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.library.utils.AUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiSuffocationPLayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        APlugin plugin = APlugin.getInstance();
        FileConfiguration configuration = plugin.getConfig();
        long lastRegen = configuration.getLong("lastRegenDate");
        long lastLogout = event.getPlayer().getLastPlayed();

        if (lastLogout < lastRegen) {
            Location teleport = this.getTeleportLocation(configuration);
            if (teleport != null) {
                Location lastLocationPlayer = event.getPlayer().getLocation();
                boolean teleported = event.getPlayer().teleport(teleport);
                if (teleported) {
                    String message = ALanguage.getMessage("TELEPORT_AFTER_REGEN",
                            lastLocationPlayer.getBlockX(),
                            lastLocationPlayer.getBlockY(),
                            lastLocationPlayer.getBlockZ(),
                            lastLocationPlayer.getWorld().getName());
                    AUtils.sendMessage(event.getPlayer(), message);
                }
            }
        }
    }

    private Location getTeleportLocation(FileConfiguration configuration) {
        double x = configuration.getDouble("safeOfSuffocation.position.x");
        double y = configuration.getDouble("safeOfSuffocation.position.y");
        double z = configuration.getDouble("safeOfSuffocation.position.z");
        String worldName = configuration.getString("safeOfSuffocation.position.world");
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return new Location(world, x, y, z);
        } else {
            APlugin.getInstance().warn("Administrator please check the world name " +
                    "in the config.yml, the world {} does not exist", worldName);
            return null;
        }
    }
}
