package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Abstract class that implement the basic thread synchronization with
 * the Orchestrator runnable.
 */
public abstract class SynchronizeRunnable extends BukkitRunnable {

    private final OrchestratorRegenRunnable orchestrator;

    public SynchronizeRunnable(OrchestratorRegenRunnable orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        orchestrator.getLock().lock();
        try {
            this.execute();
            this.orchestrator.getCondition().signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            orchestrator.getLock().unlock();
        }
    }

    /**
     * Execute the logic of the runnable. The code of this method will be
     * locked. Then the the process will throw a signal to the orchestrator to
     * continue.
     */
    protected abstract void execute();
}
