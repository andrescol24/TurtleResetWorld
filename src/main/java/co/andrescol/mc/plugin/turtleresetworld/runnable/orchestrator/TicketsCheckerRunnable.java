package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;

public class TicketsCheckerRunnable extends SynchronizeRunnable {

    private final long millis;

    public TicketsCheckerRunnable(OrchestratorRunnable orchestrator, long millis) {
        super(orchestrator);
        this.millis = millis;
    }

    @Override
    protected void execute() throws Exception {
        Thread.sleep(millis);
    }

    @Override
    public long getDelay() {
        return 0;
    }
}
