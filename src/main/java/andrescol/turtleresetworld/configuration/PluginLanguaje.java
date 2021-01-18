/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package andrescol.turtleresetworld.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.bukkit.ChatColor;

import andrescol.turtleresetworld.SimplePlugin;

/**
 * Singleton that contains the languaje configuration
 * 
 * @author andrescol24
 */

public class PluginLanguaje {

	private SimplePlugin plugin;

	/**
	 * Create the instance
	 * 
	 * @param plugin
	 */
	private PluginLanguaje(SimplePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Get the languaje property
	 * 
	 * @param key key of the propertie
	 * @return property
	 */
	private String getString(String key) {
		Properties properties = new Properties();
		File langFile = new File(plugin.getPluginFolder(), "lang.properties");
		try (InputStream input = new FileInputStream(langFile)) {
			properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
			return properties.getProperty(key);
		} catch (IOException e) {
			plugin.error("Can not load the languaje property: " + key, e);
		}
		return null;
	}

	/**
	 * Get a complete message replacing the parameters
	 * 
	 * @param property     the property
	 * @param replacements arguments
	 * @return Message
	 */
	public String getMessage(LanguajeProperty property, Object... replacements) {
		String key = property.getKey();
		String message = this.getString(key);
		if (message != null) {
			message = replaceValues(message, replacements);
			message = ChatColor.translateAlternateColorCodes('&', message);
			return message;
		}
		message = replaceValues("Please check the {} languaje configuration", key);
		return message;

	}

	// ============================ STATICS ===========================

	private static PluginLanguaje instance;

	public static PluginLanguaje getInstance(SimplePlugin plugin) {
		if (instance == null) {
			instance = new PluginLanguaje(plugin);
		}
		return instance;
	}

	/**
	 * Enum for the list of messages
	 * 
	 * @author andrescol24
	 *
	 */
	public enum LanguajeProperty {
		// TODO
		TODO;

		public String getKey() {
			return this.name().replace("_", ".").toLowerCase();
		}
	}

	/**
	 * Replace all {} values
	 * 
	 * @param message      message
	 * @param replacements replacements
	 * @return String values replaced
	 */
	public static String replaceValues(String message, Object... replacements) {
		for (Object replace : replacements) {
			message = message.replaceFirst("\\{\\}", String.valueOf(replace));
		}
		return message;
	}
}
