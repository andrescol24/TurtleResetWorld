package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.configuration.ALanguage;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.RegionFilesProcess;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class CalcTimeRegenSubCommand extends ASubCommand {

    public CalcTimeRegenSubCommand() {
        super("calculate", "turtleresetworld.calculate");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // Get Worlds to reset
        String worldParam = AUtils.getArgument(1, args);
        List<World> worlds = RegenWorldSubCommand.PARAM_ALL.equals(worldParam)
                ? Bukkit.getWorlds()
                : List.of(Bukkit.getWorld(worldParam));

        List<ChunkInFile> chunksToRegen = new LinkedList<>();
        for(World world : worlds) {
            RegionFilesProcess process = new RegionFilesProcess(world);
            process.run(false);
            chunksToRegen.addAll(process.getChunksToRegen());
        }

        int totalChunks = chunksToRegen.size();
        int executableSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
        int numberExecutables = totalChunks / executableSize + 1;
        int executableTime = executableSize / 8; // 8 chunks per second
        int executeTime = numberExecutables * executableTime;

        int graceTime = APlugin.getInstance().getConfig().getInt("timeOfGraceForServer.chunkRegen");
        graceTime = (graceTime / 20) * numberExecutables; // 20 tics per second

        // World creation + grace time + executables time + other time
        double total = 30 * worlds.size() + graceTime + executeTime + 60;

        String message;
        if(total > 3600) {
            // hours
            total = total / 3600;
            message = ALanguage.getMessage("CALCULATE_TIME_HOURS", totalChunks, total);
        } else {
            // Minutes
            total = total / 60;
            message = ALanguage.getMessage("CALCULATE_TIME_MINUTES", totalChunks, total);
        }
        AUtils.sendMessage(commandSender, message);
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
