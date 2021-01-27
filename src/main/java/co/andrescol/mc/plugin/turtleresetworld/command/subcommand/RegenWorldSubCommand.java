package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import java.util.LinkedList;
import java.util.List;

import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.runnable.ClearRegionsRunnable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.configuration.ALanguageDirectAccess;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.listener.PlayerJoinListener;

public class RegenWorldSubCommand extends ASubCommand {

    public static final String PARAM_ALL = "all";

    public RegenWorldSubCommand() {
        super("regen", "turtleresetworld.regen");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Add a listener to prevent player's join temporally
        APlugin plugin = APlugin.getInstance();
        plugin.info("Starting worlds regeneration");

        PlayerJoinListener listener = new PlayerJoinListener();
        plugin.getServer().getPluginManager().registerEvents(listener, APlugin.getInstance());

        // Kick all players
        String message = ALanguageDirectAccess.getInstance().getMessage("kick_message_player_connected");
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(message));

        // Get Worlds to reset
        String worldParam = AUtils.getArgument(1, args);
        List<World> worlds = PARAM_ALL.equals(worldParam) ? Bukkit.getWorlds() : List.of(Bukkit.getWorld(worldParam));

        ClearRegionsRunnable runnable = new ClearRegionsRunnable(worlds);
        runnable.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new LinkedList<>();
        if (args.length <= 2) {
            String name = args.length == 2 ? args[1] : "";
            List<World> worlds = Bukkit.getWorlds();
            worlds.forEach(x -> {
                if (x.getName().startsWith(name)) {
                    list.add(x.getName());
                }
            });
            if ("all".startsWith(name)) {
                list.add("all");
            }
        }
        return list;
    }

    @Override
    public boolean goodUsage(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            World world = Bukkit.getWorld(args[1]);
            return world != null || "all".equals(args[1]);
        }
        return false;
    }
}
