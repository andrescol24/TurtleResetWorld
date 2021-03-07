package co.andrescol.mc.plugin.turtleresetworld.runnable.postregen;

import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator.OrchestratorRunnable;

public class LoadSchematicRunnable extends SynchronizeRunnable {

    public LoadSchematicRunnable(OrchestratorRunnable orchestrator) {
        super(orchestrator);
    }

    @Override
    protected void execute() {

    }

    @Override
    public long getDelay() {
        return 0;
    }
}
