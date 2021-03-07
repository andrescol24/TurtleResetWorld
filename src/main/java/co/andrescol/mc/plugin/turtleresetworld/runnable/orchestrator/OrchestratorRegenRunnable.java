package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.FileName;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.beforeregen.CreateWorldRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.DeleteWorldRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.UnRegisterEventRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.RegenChunkRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.WorldFilesProcess;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OrchestratorRegenRunnable extends OrchestratorRunnable {

    private final List<World> worldsToRegen;
    private final AntiPlayerJoinListener listener;

    /**
     * Create an instance of this orchestrator
     *
     * @param worldsToRegen Worlds to regen
     * @param listener      AntiPlayerJoinListener that going to be unregister after the regneration
     */
    public OrchestratorRegenRunnable(List<World> worldsToRegen, AntiPlayerJoinListener listener) {
        super();
        this.worldsToRegen = worldsToRegen;
        this.listener = listener;
        this.totalChunks = 0;
    }

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();
        this.lock.lock();
        try {
            this.executeTasks();
        } catch (Exception e) {
            plugin.error("There was an error running the OrchestratorRegen", e);
        } finally {
            this.lock.unlock();
        }
        UnRegisterEventRunnable unRegisterEventRunnable = new UnRegisterEventRunnable(listener);
        unRegisterEventRunnable.runTask(plugin);
    }

    /**
     * This method run all of task to regenerate all worlds.
     * First it going to delete the region file without claimed chunks using the class {@link WorldFilesProcess}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     *
     */
    private void executeTasks() throws InterruptedException {
        APlugin plugin = APlugin.getInstance();
        RegenerationDataManager dataManager = RegenerationDataManager
                .getInstance(FileName.FILE_NAME_REGEN);

        for (World world : this.worldsToRegen) {
            // We get the chunks to save the schematic
            WorldRegenerationData data = dataManager.getDataOf(world);
            WorldFilesProcess worldProcess = new WorldFilesProcess(world);
            List<ChunkInFile> chunksForSave;
            if(data.getChunksToSaveSchematic().isEmpty()) {
                chunksForSave = worldProcess.getProtectedChunks();
            } else {
                chunksForSave = data.getChunksToSaveSchematic();
            }
            worldProcess.deleteRegionsUnclaimed();
//            List<ChunkInFile> chunkToRegen = filesResult.getChunksToRegen();
//            this.totalChunks = this.totalChunks + chunkToRegen.size();
//            dataManager.addChunks(world, chunkToRegen);
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
}
