package andrescol.turtleresetworld.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import andrescol.turtleresetworld.SimplePlugin;

public class TurtleResetWorldCommand extends SimpleCommand {

	public TurtleResetWorldCommand(SimplePlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.hasPermission(command.getPermission())) {
			this.sendMessage(sender, "Hello from Turtle Plugin, reset your world!");
			return true;
		}
		return false;
	}

}
