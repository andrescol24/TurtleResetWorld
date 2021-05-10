package co.andrescol.mc.plugin.turtleresetworld.listener;

import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.andrescol.mc.library.configuration.ALanguage;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class AntiPlayerJoinListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
		List<World> worlds = dataManager.getListWorldsPending();
		int total = 0;
		for(World world : worlds) {
			WorldRegenerationData data = dataManager.getDataOf(world);
			total += data.getChunksToLoadSchematic().size();
			total += data.getChunksToSaveSchematic().size();
		}
		String message = ALanguage.getMessage("KICK_MESSAGE_ONLOGIN", total);
		Player player = event.getPlayer();
		player.kickPlayer(message);
	}
}
