package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.configuration.ALanguage;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.OrchestratorRegenRunnable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class CreateWorldSubCommand extends ASubCommand {

    public static final String PARAM_ALL = "all";

    protected CreateWorldSubCommand() {
        super("create", "turtleresetworld.create");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        APlugin plugin = APlugin.getInstance();
        // Add a listener to prevent player's join temporally
        AntiPlayerJoinListener listener = new AntiPlayerJoinListener();
        plugin.getServer().getPluginManager().registerEvents(listener, APlugin.getInstance());

        // Kick all players
        String message = ALanguage.getMessage("KICK_MESSAGE");
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(message));

        // Get Worlds to reset
        String worldParam = AUtils.getArgument(1, args);
        List<World> worlds = PARAM_ALL.equals(worldParam)
                ? Bukkit.getWorlds()
                : List.of(Bukkit.getWorld(worldParam));
        OrchestratorRegenRunnable runnable = new OrchestratorRegenRunnable(worlds);
        runnable.runTaskAsynchronously(plugin);
        plugin.info("Starting worlds regeneration");
        return true;
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 2) {
            World world = Bukkit.getWorld(args[1]);
            return world != null || "all".equals(args[1]);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
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
}
