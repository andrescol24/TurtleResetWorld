package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class OrchestratorRunnable extends BukkitRunnable {

    protected final Lock lock;
    protected final Condition condition;
    protected Integer totalChunks;

    protected OrchestratorRunnable() {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.totalChunks = 0;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getCondition() {
        return condition;
    }
}
