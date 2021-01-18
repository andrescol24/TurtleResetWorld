package andrescol.turtleresetworld;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import andrescol.turtleresetworld.command.TurtleResetWorldCommand;
import andrescol.turtleresetworld.configuration.PluginLanguaje;

public class TurtleResetWorldPlugin extends JavaPlugin implements SimplePlugin{
	
	@Override
	public void onEnable() {
		this.getCommand("turtle").setExecutor(new TurtleResetWorldCommand(this));
		this.chargeConfiguration();
	}

	@Override
	public void onDisable() {
		// TODO
		HandlerList.unregisterAll(this);
	}

	@Override
	public void info(String message, Object... replacements) {
		message = PluginLanguaje.replaceValues(message, replacements);
		this.getLogger().info(message);
	}

	@Override
	public void warn(String message, Object... replacements) {
		message = PluginLanguaje.replaceValues(message, replacements);
		this.getLogger().warning(message);

	}

	@Override
	public void error(String message, Throwable exception, Object... replacements) {
		message = PluginLanguaje.replaceValues(message, replacements);
		this.getLogger().log(Level.SEVERE, message, exception);

	}

	@Override
	public FileConfiguration getFileConfiguration() {
		return this.getConfig();
	}

	@Override
	public File getPluginFolder() {
		return this.getDataFolder();
	}

	/**
	 * Load and save the configuration files
	 */
	private void chargeConfiguration() {
		getConfig().options().copyDefaults(true);
		File config = new File(getDataFolder(), "config.yml");
		File lang = new File(getDataFolder(), "lang.properties");
		try {
			if (!config.exists()) {
				saveDefaultConfig();
			}
			if (!lang.exists()) {
				saveResource("lang.properties", false);
			}
		} catch (Exception e) {
			this.error("Can not load the configuration", e);
		}
	}
}
