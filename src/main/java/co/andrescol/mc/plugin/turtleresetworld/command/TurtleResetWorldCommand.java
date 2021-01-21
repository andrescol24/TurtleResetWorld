package co.andrescol.mc.plugin.turtleresetworld.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.andrescol.mc.library.configuration.ALanguageDirectAccess;
import co.andrescol.mc.library.utils.AUtils;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.enums.MessageName;

public class TurtleResetWorldCommand implements CommandExecutor {

	private final TurtleResetWorldPlugin plugin;

	public TurtleResetWorldCommand(TurtleResetWorldPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission(command.getPermission())) {
			String message = ALanguageDirectAccess.getInstance().getMessage(MessageName.MESSAGE,
					sender.getName());
			String version = this.plugin.getConfig().getString("version");
			this.plugin.info("the configuration: {}", version);
			AUtils.sendMessage(sender, message, plugin);
			if(args.length > 0) {
				this.plugin.reload();
				this.plugin.info("reloaded...");
			}
			return true;
		}
		return false;
	}

}
