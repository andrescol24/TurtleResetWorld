package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.RegionFilesProcess;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrchestratorRegenRunnable extends BukkitRunnable {

    private final Lock lock;
    private final Condition condition;
    private final List<World> worldsToRegen;

    public OrchestratorRegenRunnable(List<World> worldsToRegen) {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.worldsToRegen = worldsToRegen;
    }

    @Override
    public void run() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        plugin.info("Starting regeneration of worlds");
        try {
            this.executeTasks();
        } catch (Exception e) {
            plugin.error("Error running the regeneration thread", e);
        } finally {
            this.lock.unlock();
        }
        plugin.info("The regeneration has Finished!");
        RestartServerRunnable restartServerRunnable = new RestartServerRunnable();
        restartServerRunnable.runTask(plugin);
    }

    /**
     * This method run all of task to regenerate all worlds.
     * First it going to delete the region file without claimed chunks using the class {@link RegionFilesProcess}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     *
     * @throws InterruptedException Throws if there is an error waiting other thread
     */
    private void executeTasks() throws InterruptedException {
        APlugin plugin = APlugin.getInstance();

        Queue<SynchronizeRunnable> executables = new LinkedList<>();
        for (World world : this.worldsToRegen) {
            RegionFilesProcess filesResult = new RegionFilesProcess(world);
            filesResult.run();
            List<ChunkInFile> chunkToRegen = filesResult.getChunksToRegen();
            List<SynchronizeRunnable> executablesForWorld = this.getWorldExecutables(world, chunkToRegen);
            executables.addAll(executablesForWorld);
        }

        long delay = plugin.getConfig().getLong("timeOfGraceForServer.chunkRegen");
        plugin.info("It is going to run {} task with {} tickets of delay", executables.size(), delay);

        for (SynchronizeRunnable task : executables) {
            task.runTaskLater(plugin, delay);
            this.condition.await();
        }
    }

    /**
     * Get the list of executables. This method also create the temporal world because
     * it is necessary for the other runnable
     *
     * @param world         world to regen
     * @param chunksToRegen List of chunks to regen
     * @return List of executables
     * @throws InterruptedException It will throw it if there is an error waiting the
     *                              temporal world creating
     */
    private List<SynchronizeRunnable> getWorldExecutables(World world, List<ChunkInFile> chunksToRegen)
            throws InterruptedException {
        APlugin plugin = APlugin.getInstance();
        List<SynchronizeRunnable> executables = new LinkedList<>();
        if (!chunksToRegen.isEmpty()) {
            // Creates the temporal world
            CreateWorldRunnable createWorldRunnable = new CreateWorldRunnable(this, world);
            long delayWorld = plugin.getConfig().getLong("timeOfGraceForServer.worldCreating");
            createWorldRunnable.runTaskLater(plugin, delayWorld);
            this.condition.await();

            // Adds executables for chunks and delete world
            if (createWorldRunnable.getClone() != null) {
                List<RegenChunkRunnable> regenExecutables = this.filterChunksExecutables(
                        chunksToRegen, world, createWorldRunnable.getClone());
                executables.addAll(regenExecutables);
                DeleteWorldRunnable deleteRunnable = new DeleteWorldRunnable(
                        this, createWorldRunnable.getClone());
                executables.add(deleteRunnable);
            }
        }
        return executables;
    }

    /**
     * This method filters chunks that don't have changes
     *
     * @param chunksToRegen List of chunks in the world that going to be regenerate
     * @param real          The real world
     * @param clone         The clone world
     * @return List of executables
     * @throws InterruptedException Throw if there is an error waiting the filter
     */
    private List<RegenChunkRunnable> filterChunksExecutables(List<ChunkInFile> chunksToRegen, World real,
                                                             World clone) throws InterruptedException {
        List<FilterChunksRunnable> executables = this.splitChunks(
                FilterChunksRunnable.class, chunksToRegen, real, clone);

        List<ChunkInFile> totalChunks = new LinkedList<>();
        int delayFilter = APlugin.getInstance().getConfig().getInt("timeOfGraceForServer.filterChunk");
        for (FilterChunksRunnable runnable : executables) {
            runnable.runTaskLater(APlugin.getInstance(), delayFilter);
            this.condition.await();
            totalChunks.addAll(runnable.getChunksToRegen());
        }
        List<RegenChunkRunnable> result = this.splitChunks(RegenChunkRunnable.class, totalChunks, real, clone);
        APlugin.getInstance().info("----- chunk to regen: {} in {} executables -----",
                totalChunks.size(), result.size());
        return result;
    }

    /**
     * This method spit the chunksToRegen in several {@link FilterChunksRunnable} or {@link RegenChunkRunnable} with
     * config value <strong>chunksPerThread</strong> size
     *
     * @param type          Type of list. Only can be {@link FilterChunksRunnable} or {@link RegenChunkRunnable}
     * @param chunksToRegen Array to split
     * @param real          real world
     * @param clone         Clone World
     * @return list of executables each with its list of chunks to filter
     */
    @SuppressWarnings("unchecked")
    private <T extends SynchronizeRunnable> List<T> splitChunks(
            Class<T> type, List<ChunkInFile> chunksToRegen, World real, World clone) {

        List<T> executables = new LinkedList<>();
        ConcurrentLinkedDeque<ChunkInFile> chunksSplit = new ConcurrentLinkedDeque<>();
        int i = 0;
        int splitSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
        for (ChunkInFile chunkInFile : chunksToRegen) {
            if (i < splitSize) {
                chunksSplit.add(chunkInFile);
                i++;
            } else {
                SynchronizeRunnable runnable =
                        type == FilterChunksRunnable.class
                                ? new FilterChunksRunnable(this, chunksSplit, real, clone)
                                : new RegenChunkRunnable(this, real, clone, chunksSplit);
                executables.add((T) runnable);
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            SynchronizeRunnable runnable =
                    type == FilterChunksRunnable.class
                            ? new FilterChunksRunnable(this, chunksSplit, real, clone)
                            : new RegenChunkRunnable(this, real, clone, chunksSplit);
            executables.add((T) runnable);
        }
        return executables;
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getCondition() {
        return condition;
    }
}
