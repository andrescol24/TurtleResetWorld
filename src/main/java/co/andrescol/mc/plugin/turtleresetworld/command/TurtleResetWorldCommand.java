package co.andrescol.mc.plugin.turtleresetworld.command;

import java.util.List;

import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.CalcTimeRegenSubCommand;
import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.CreateWorldSubCommand;
import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.RegenWorldSubCommand;
import co.andrescol.mc.plugin.turtleresetworld.command.subcommand.TpSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import co.andrescol.mc.library.command.AMainCommand;

public class TurtleResetWorldCommand extends AMainCommand {

	public TurtleResetWorldCommand() {
		this.addSubCommand(new RegenWorldSubCommand());
		this.addSubCommand(new TpSubCommand());
		this.addSubCommand(new CalcTimeRegenSubCommand());
		this.addSubCommand(new CreateWorldSubCommand());
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
