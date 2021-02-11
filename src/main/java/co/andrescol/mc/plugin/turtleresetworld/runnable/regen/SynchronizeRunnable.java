package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Abstract class that implement the basic thread synchronization with
 * the Orchestrator runnable.
 */
public abstract class SynchronizeRunnable extends BukkitRunnable {

    protected final OrchestratorRegenRunnable orchestrator;

    public SynchronizeRunnable(OrchestratorRegenRunnable orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        orchestrator.getLock().lock();
        try {
            this.execute();
        } catch (Exception e) {
            APlugin.getInstance().error("Error executing runnable {}", e, this);
        } finally {
            this.orchestrator.getCondition().signal();
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
