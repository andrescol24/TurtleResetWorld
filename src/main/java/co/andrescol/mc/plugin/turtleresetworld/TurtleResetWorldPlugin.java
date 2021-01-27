package co.andrescol.mc.plugin.turtleresetworld;

import co.andrescol.mc.plugin.turtleresetworld.listener.AntiSuffocationPLayerJoinListener;
import org.bukkit.event.HandlerList;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.command.TurtleResetWorldCommand;

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
}
