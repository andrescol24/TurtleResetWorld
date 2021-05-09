package co.andrescol.mc.plugin.turtleresetworld.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import co.andrescol.mc.library.configuration.ALanguage;
import org.bukkit.event.player.PlayerLoginEvent;

public class AntiPlayerJoinListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event) {
		String message = ALanguage.getMessage("KICK_MESSAGE");
		Player player = event.getPlayer();
		player.kickPlayer(message);
	}
}
