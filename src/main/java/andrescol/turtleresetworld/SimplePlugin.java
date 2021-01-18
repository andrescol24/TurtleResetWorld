package andrescol.turtleresetworld;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Interface that abstract public methods that can be use across the plugin
 * 
 * @author andrescol24
 *
 */
public interface SimplePlugin {

	/**
	 * Write a console message
	 * 
	 * @param message the message
	 */
	void info(String message, Object... replacements);

	/**
	 * Write a console warning message
	 * 
	 * @param message Message to write
	 */
	void warn(String message, Object... replacements);

	/**
	 * Write a severe error to console
	 * 
	 * @param message   Message
	 * @param exception the throws exception
	 */
	void error(String message, Throwable exception, Object... replacements);

	/**
	 * Get the file configuration
	 * 
	 * @return the configuration
	 */
	FileConfiguration getFileConfiguration();

	/**
	 * Get the folder plugin
	 * 
	 * @return Plugin's folder
	 */
	File getPluginFolder();

	/**
	 * Reload the configuration
	 */
	void reloadConfig();
}
