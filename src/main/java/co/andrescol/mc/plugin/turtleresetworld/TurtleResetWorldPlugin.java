package co.andrescol.mc.plugin.turtleresetworld;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.command.TurtleResetWorldCommand;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import co.andrescol.mc.plugin.turtleresetworld.hooks.GriefPreventionClaimer;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiSuffocationPLayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator.OrchestratorLoadSchematicsRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.List;

public class TurtleResetWorldPlugin extends APlugin {
	
	@Override
	public void onEnable() {
		this.getCommand("turtle").setExecutor(new TurtleResetWorldCommand());
		if (this.getConfig().getBoolean("safeOfSuffocation.enable")) {
			AntiSuffocationPLayerJoinListener listener = new AntiSuffocationPLayerJoinListener();
			this.getServer().getPluginManager().registerEvents(listener, this);
		}
		RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
		if (dataManager.isContinueLoading()) {
			AntiPlayerJoinListener listener = new AntiPlayerJoinListener();
			this.getServer().getPluginManager().registerEvents(listener, this);
			OrchestratorLoadSchematicsRunnable runnable = new OrchestratorLoadSchematicsRunnable(listener);
			runnable.runTaskLaterAsynchronously(this, 200);
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
			claimers.add(new GriefPreventionClaimer());
		}
		List<Chunk> chunks = new LinkedList<>();
		claimers.forEach(claimer -> chunks.addAll(claimer.getClaimedChunks()));
		return chunks;
	}
}
