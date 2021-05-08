package co.andrescol.mc.plugin.turtleresetworld.listener;

import co.andrescol.mc.library.configuration.ALanguage;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class AntiSuffocationPlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        APlugin plugin = APlugin.getInstance();

        RegenerationDataManager dataManager = RegenerationDataManager
                .getInstance();
        String nick = event.getPlayer().getName();
        if(!dataManager.isTeleportedPlayer(nick)) {
            FileConfiguration configuration = plugin.getConfig();
            Location teleport = this.getTeleportLocation(configuration);
            if (teleport != null) {
                Location lastLocationPlayer = event.getPlayer().getLocation();
                boolean teleported = event.getPlayer().teleport(teleport);
                if (teleported) {
                    String message = ALanguage.getMessage("TELEPORT_AFTER_REGEN",
                            lastLocationPlayer.getBlockX(),
                            lastLocationPlayer.getBlockY(),
                            lastLocationPlayer.getBlockZ(),
                            Objects.requireNonNull(lastLocationPlayer.getWorld()).getName());
                    AUtils.sendMessage(event.getPlayer(), message);
                    dataManager.addTeleportedPlayer(nick);
                }
            }
        }
    }

    private Location getTeleportLocation(FileConfiguration configuration) {
        double x = configuration.getDouble("safeOfSuffocation.position.x");
        double y = configuration.getDouble("safeOfSuffocation.position.y");
        double z = configuration.getDouble("safeOfSuffocation.position.z");
        float yaw = (float) configuration.getDouble("safeOfSuffocation.position.yaw");
        float pitch = (float) configuration.getDouble("safeOfSuffocation.position.pitch");
        String worldName = configuration.getString("safeOfSuffocation.position.world");
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            APlugin.getInstance().warn("Administrator please check the world name " +
                    "in the config.yml, the world {} does not exist", worldName);
            return null;
        }
    }
}
