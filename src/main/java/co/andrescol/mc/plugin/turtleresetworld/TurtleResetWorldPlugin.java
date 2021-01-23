package co.andrescol.mc.plugin.turtleresetworld;

import org.bukkit.event.HandlerList;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.command.TurtleResetWorldCommand;

public class TurtleResetWorldPlugin extends APlugin{
	
	@Override
	public void onEnable() {
		this.getCommand("turtle").setExecutor(new TurtleResetWorldCommand());
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		this.getServer().getScheduler().cancelTasks(this);
	}
}
