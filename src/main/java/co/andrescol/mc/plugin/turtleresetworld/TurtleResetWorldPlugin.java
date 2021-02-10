package co.andrescol.mc.plugin.turtleresetworld;

import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import co.andrescol.mc.plugin.turtleresetworld.hooks.GriefPreventionClaimer;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiSuffocationPLayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.HandlerList;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.command.TurtleResetWorldCommand;

import java.util.LinkedList;
import java.util.List;

public class TurtleResetWorldPlugin extends APlugin{
	
	@Override
	public void onEnable() {
		this.getCommand("turtle").setExecutor(new TurtleResetWorldCommand());
		if(this.getConfig().getBoolean("safeOfSuffocation.enable")) {
			AntiSuffocationPLayerJoinListener listener = new AntiSuffocationPLayerJoinListener();
			this.getServer().getPluginManager().registerEvents(listener, this);
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		this.getServer().getScheduler().cancelTasks(this);
	}

	/**
	 * Charges the hooks
	 *
	 * @return list of claimers for protect chunks claimed
	 */
	public static List<Chunk> getChunksClaimedByHooks() {
		List<Claimer> claimers = new LinkedList<>();
		if (Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
			APlugin.getInstance().info("There is GriefPrevention plugin!");
			claimers.add(new GriefPreventionClaimer());
		}
		List<Chunk> chunks = new LinkedList<>();
		claimers.forEach(claimer -> chunks.addAll(claimer.getClaimedChunks()));
		return chunks;
	}
}
