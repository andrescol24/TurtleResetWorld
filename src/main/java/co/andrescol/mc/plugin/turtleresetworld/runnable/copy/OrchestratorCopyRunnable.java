package co.andrescol.mc.plugin.turtleresetworld.runnable.copy;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.FileName;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.CreateWorldRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.WorldFilesProcess;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OrchestratorCopyRunnable extends OrchestratorRunnable {

    private final List<World> worldsToRegen;
    private final AntiPlayerJoinListener listener;

    public OrchestratorCopyRunnable(List<World> worldsToRegen, AntiPlayerJoinListener listener) {
        this.worldsToRegen = worldsToRegen;
        this.listener = listener;
    }

    @Override
    public void run() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        plugin.info("!!!! Starting worlds regeneration: Copy claims to clone worlds!!!!");
        try {
            this.executeTasks();
            plugin.info("The regeneration has Finished!");
        } catch (Exception e) {
            plugin.error("Error running the regeneration thread", e);
        } finally {
            this.lock.unlock();
        }
        UnRegisterEventRunnable unRegisterEventRunnable = new UnRegisterEventRunnable(this.listener);
        unRegisterEventRunnable.runTaskLater(plugin, 50L);
    }

    /**
     * This method run all of task to regenerate all worlds.
     * First it going to delete the region file without claimed chunks using the class {@link WorldFilesProcess}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     */
    private void executeTasks() throws InterruptedException {
        APlugin plugin = APlugin.getInstance();

        this.totalChunks = 0;
        Queue<SynchronizeRunnable> executables = new LinkedList<>();
        RegenerationDataManager dataManager = RegenerationDataManager
                .getInstance(FileName.FILE_NAME_COPY);

        for (World world : this.worldsToRegen) {
            // Delete region files that does not have claims
            WorldFilesProcess filesResult = new WorldFilesProcess(world);
            filesResult.deleteRegionsUnclaimed();
            List<ChunkInFile> chunkToCopy = filesResult.getProtectedChunks();

            // Load and save the regeneration information
            this.totalChunks = this.totalChunks + chunkToCopy.size();
            WorldRegenerationData data = dataManager.getDataOf(world);
            if(data.getChunksToRegen().isEmpty()) {
                dataManager.addChunks(world, chunkToCopy);
            }
            // Gets executables for copy
            List<CopyChunkRunnable> executablesForWorld = this.getWorldExecutables(world, chunkToCopy);
            executables.addAll(executablesForWorld);
        }

        plugin.info("\n------ Starting regeneration: chunks to regen {} on {} process---------",
                this.totalChunks, executables.size());

        long delay = 200L;
        for (SynchronizeRunnable task : executables) {
            task.runTaskLater(plugin, delay);
            this.condition.await();
            delay = task.getDelay();

            if (task instanceof CopyChunkRunnable) {
                CopyChunkRunnable runnable = (CopyChunkRunnable) task;
                dataManager.removeChunks(runnable.getFrom(), runnable.getChunks());
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
    private List<CopyChunkRunnable> getWorldExecutables(World world, List<ChunkInFile> chunksToRegen)
            throws InterruptedException {
        APlugin plugin = APlugin.getInstance();
        List<CopyChunkRunnable> executables = new LinkedList<>();
        if (!chunksToRegen.isEmpty()) {
            // Creates the temporal world
            CreateWorldRunnable createWorldRunnable = new CreateWorldRunnable(this, world);
            createWorldRunnable.runTask(plugin);
            this.condition.await();

            // Adds executables for chunks and delete world
            if (createWorldRunnable.getClone() != null) {
                List<CopyChunkRunnable> copyExecutables = this.splitChunks(chunksToRegen, createWorldRunnable.getClone(), world);
                executables.addAll(copyExecutables);
            }
        }
        return executables;
    }

    /**
     * This method spit the chunks to copy in several {@link CopyChunkRunnable} with
     * config value <strong>chunksPerThread</strong> size
     *
     * @param chunksToCopy Array to split
     * @param to          real world
     * @param from         Clone World
     * @return list of executables each with its list of chunks to filter
     */
    private List<CopyChunkRunnable> splitChunks(List<ChunkInFile> chunksToCopy, World to, World from) {

        List<CopyChunkRunnable> executables = new LinkedList<>();
        ConcurrentLinkedDeque<ChunkInFile> chunksSplit = new ConcurrentLinkedDeque<>();
        int i = 0;
        int splitSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
        for (ChunkInFile chunkInFile : chunksToCopy) {
            chunksSplit.add(chunkInFile);
            if (i < splitSize - 1) {
                i++;
            } else {
                CopyChunkRunnable runnable = new CopyChunkRunnable(this, to, from, chunksSplit);
                executables.add(runnable);
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            CopyChunkRunnable runnable = new CopyChunkRunnable(this, to, from, chunksSplit);
            executables.add(runnable);
        }
        return executables;
    }
}
