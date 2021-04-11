package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.LoadSchematicRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.SaveSchematicChunkRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
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

    /**
     * Split the list of chunks in {@link LoadSchematicRunnable} or {@link SaveSchematicChunkRunnable} list
     *
     * @param type   {@link LoadSchematicRunnable} or {@link SaveSchematicChunkRunnable}
     * @param chunks list of chunks to split
     * @param world  the world of the chunks
     * @param <T>    Type
     * @return A list with the {@link SynchronizeRunnable} objects to run
     */
    @SuppressWarnings("unchecked")
    protected <T extends SynchronizeRunnable> List<T> splitChunks(Class<T> type, List<ChunkInFile> chunks, World world) {
        List<T> executables = new LinkedList<>();
        ConcurrentLinkedDeque<ChunkInFile> chunksSplit = new ConcurrentLinkedDeque<>();
        int i = 0;
        int splitSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
        for (ChunkInFile chunkInFile : chunks) {
            chunksSplit.add(chunkInFile);
            if (i < splitSize - 1) {
                i++;
            } else {
                SynchronizeRunnable runnable = type == LoadSchematicRunnable.class
                        ? new LoadSchematicRunnable(this, chunksSplit, world)
                        : new SaveSchematicChunkRunnable(this, chunksSplit, world);
                executables.add((T) runnable);
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            SynchronizeRunnable runnable = type == LoadSchematicRunnable.class
                    ? new LoadSchematicRunnable(this, chunksSplit, world)
                    : new SaveSchematicChunkRunnable(this, chunksSplit, world);
            executables.add((T) runnable);
        }
        return executables;
    }
}