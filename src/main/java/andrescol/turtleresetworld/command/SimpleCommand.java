package andrescol.turtleresetworld.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import andrescol.turtleresetworld.SimplePlugin;

/**
 * Superclass that define utils methods
 * 
 * @author andrescol24
 *
 */
public abstract class SimpleCommand implements CommandExecutor {

	protected SimplePlugin plugin;

	/**
	 * Initialize the instance
	 * 
	 * @param plugin
	 */
	protected SimpleCommand(SimplePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * return the argument of a position
	 * 
	 * @param position position
	 * @param args     list of arguments
	 * @return Argument or null if not present
	 */
	protected String getArgument(int position, String[] args) {
		if (args != null && args.length > position) {
			return args[position];
		}
		return null;
	}
	
	protected void sendMessage(CommandSender sender, String msg) {
		if(sender instanceof Player) {
			sender.sendMessage(msg);
		} else {
			plugin.info(msg);
		}
	}
}
