package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import org.bukkit.Server;

public class TicketsCheckerRunnable extends SynchronizeRunnable {

    public TicketsCheckerRunnable(OrchestratorRunnable orchestrator) {
        super(orchestrator);
    }

    @Override
    protected void execute() throws Exception {
        APlugin plugin = APlugin.getInstance();
        Server server = plugin.getServer();
        server.dispatchCommand(server.getConsoleSender(), "turtle tps");
    }
}
