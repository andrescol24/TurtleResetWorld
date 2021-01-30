package co.andrescol.mc.plugin.turtleresetworld.command;

import java.util.List;

import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.RegenChunkSubCommand;
import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.RegenWorldSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import co.andrescol.mc.library.command.AMainCommand;

public class TurtleResetWorldCommand extends AMainCommand {

	public TurtleResetWorldCommand() {
		this.addSubCommand(new RegenWorldSubCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return this.handle(sender, command, label, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return this.completeTab(sender, command, alias, args);
	}

}
