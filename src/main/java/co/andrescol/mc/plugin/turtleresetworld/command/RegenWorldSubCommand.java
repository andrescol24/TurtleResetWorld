package co.andrescol.mc.plugin.turtleresetworld.command;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.utils.AUtils;

public class RegenWorldSubCommand extends ASubCommand {

	protected RegenWorldSubCommand() {
		super("regen", "turtleresetworld.regen");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new LinkedList<>();
		if (sender.hasPermission(this.getPermission())) {
			String name = args.length == 2 ? args[1] : "";
			List<World> worlds = Bukkit.getWorlds();
			worlds.forEach(x ->{
				if(x.getName().startsWith(name)) {
					list.add(x.getName());
				}
			});
			if("all".startsWith(name)) {
				list.add("all");
			}
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		AUtils.sendMessage(sender, "Regeneration...");
		return true;
	}

	@Override
	public boolean goodUsage(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			World world = Bukkit.getWorld(args[1]);
			if (world != null || "all".equals(args[1])) {
				return true;
			}
		}
		return false;
	}

}
