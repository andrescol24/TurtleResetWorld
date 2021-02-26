package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.ReadingDataException;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationData;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.WorldFilesProcess;
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

    protected final Lock lock;
    protected final Condition condition;
    protected final List<World> worldsToRegen;
    protected Integer totalChunks;

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
        boolean success = false;
        try {
            this.executeTasks();
            success = true;
        } catch (Exception e) {
            plugin.error("Error running the regeneration thread", e);
        } finally {
            this.lock.unlock();
        }
        plugin.info("The regeneration has Finished!");
        RestartServerRunnable restartServerRunnable = new RestartServerRunnable(success);
        restartServerRunnable.runTask(plugin);
    }

    /**
     * This method run all of task to regenerate all worlds.
     * First it going to delete the region file without claimed chunks using the class {@link WorldFilesProcess}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     *
     * @throws InterruptedException Throws if there is an error waiting other thread
     */
    private void executeTasks() throws InterruptedException, ReadingDataException {
        APlugin plugin = APlugin.getInstance();
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        RegenerationData data = dataManager.getData();

        this.totalChunks = 0;
        Queue<SynchronizeRunnable> executables = new LinkedList<>();
        for (World world : this.worldsToRegen) {
            WorldFilesProcess filesResult = new WorldFilesProcess(world);
            filesResult.run(true);
            List<ChunkInFile> chunkToRegen = filesResult.getChunksToRegen();
            this.totalChunks = this.totalChunks + chunkToRegen.size();
            data.addChunksToRegen(chunkToRegen, world);
            List<SynchronizeRunnable> executablesForWorld = this.getWorldExecutables(world, chunkToRegen);
            executables.addAll(executablesForWorld);
        }
        dataManager.saveData();

        long delay = plugin.getConfig().getLong("timeOfGraceForServer.chunkRegen");
        plugin.info("\n------ Starting regeneration: chunks to regen {} on {} process---------",
                this.totalChunks, executables.size());
        for (SynchronizeRunnable task : executables) {
            task.runTaskLater(plugin, delay);
            this.condition.await();

            if(task instanceof RegenChunkRunnable) {
                RegenChunkRunnable runnable = (RegenChunkRunnable) task;
                data.removeChunksToRegen(runnable.getChunks(), runnable.getReal());
                dataManager.saveData();
            }
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
                List<RegenChunkRunnable> regenExecutables = this.splitChunks(chunksToRegen, world, createWorldRunnable.getClone());
                executables.addAll(regenExecutables);
                DeleteWorldRunnable deleteRunnable = new DeleteWorldRunnable(
                        this, createWorldRunnable.getClone());
                executables.add(deleteRunnable);
            }
        }
        return executables;
    }

    /**
     * This method spit the chunksToRegen in several {@link RegenChunkRunnable} with
     * config value <strong>chunksPerThread</strong> size
     *
     * @param chunksToRegen Array to split
     * @param real          real world
     * @param clone         Clone World
     * @return list of executables each with its list of chunks to filter
     */
    @SuppressWarnings("unchecked")
    private <T extends SynchronizeRunnable> List<T> splitChunks(List<ChunkInFile> chunksToRegen, World real, World clone) {

        List<T> executables = new LinkedList<>();
        ConcurrentLinkedDeque<ChunkInFile> chunksSplit = new ConcurrentLinkedDeque<>();
        int i = 0;
        int splitSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
        for (ChunkInFile chunkInFile : chunksToRegen) {
            chunksSplit.add(chunkInFile);
            if (i < splitSize - 1) {
                i++;
            } else {
                SynchronizeRunnable runnable = new RegenChunkRunnable(this, real, clone, chunksSplit);
                executables.add((T) runnable);
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            SynchronizeRunnable runnable = new RegenChunkRunnable(this, real, clone, chunksSplit);
            executables.add((T) runnable);
        }
        return executables;
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
