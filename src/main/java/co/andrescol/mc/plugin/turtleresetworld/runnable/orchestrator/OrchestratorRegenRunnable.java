package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.LoadSchematicRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.RestartServerRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.UnRegisterEventRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.SaveSchematicChunkRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.WorldFilesProcess;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OrchestratorRegenRunnable extends OrchestratorRunnable {

    private final List<World> worldsToRegen;
    private final AntiPlayerJoinListener listener;

    /**
     * Create an instance of this orchestrator
     *
     * @param worldsToRegen Worlds to regen
     * @param listener      AntiPlayerJoinListener that going to be unregister after the regeneration
     */
    public OrchestratorRegenRunnable(List<World> worldsToRegen, AntiPlayerJoinListener listener) {
        super();
        this.worldsToRegen = worldsToRegen;
        this.listener = listener;
        this.totalChunks = 0;
    }

    @Override
    public void run() {
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        if (!dataManager.isContinueLoading()) {
            this.saveSchematics();
        } else {
            this.loadSchematics();
        }
    }

    private void saveSchematics() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        List<SaveSchematicChunkRunnable> executables = new LinkedList<>();
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        try {
            // List all process
            List<WorldFilesProcess> processes = new LinkedList<>();
            for (World world : this.worldsToRegen) {
                WorldFilesProcess worldProcess = new WorldFilesProcess(world);
                WorldRegenerationData data = dataManager.getDataOf(world);
                List<ChunkInFile> chunksForSave;
                if (data.getChunksToSaveSchematic().isEmpty()) {
                    chunksForSave = worldProcess.getProtectedChunks();
                    dataManager.addChunksSaveSchematics(world, chunksForSave);
                } else {
                    chunksForSave = data.getChunksToSaveSchematic();
                    plugin.info("It's going to continue with {} chunks to save their schematics",
                            chunksForSave.size());
                }
                this.totalChunks = chunksForSave.size();
                List<SaveSchematicChunkRunnable> savesExecutables = this.splitChunks
                        (SaveSchematicChunkRunnable.class, chunksForSave, world);
                executables.addAll(savesExecutables);
                processes.add(worldProcess);
            }

            // Run the executables for save the schematics of each protected chunk
            boolean success = true;
            plugin.info("Starting process to save the schematics of {} chunks", this.totalChunks);
            for (SaveSchematicChunkRunnable runnable : executables) {
                runnable.runTaskLater(plugin, runnable.getDelay());
                this.condition.await();
                if (runnable.isSuccess()) {
                    dataManager.removeChunksSaveSchematics(runnable.getWorld(), runnable.getChunks());
                } else {
                    success = false;
                    break;
                }
            }

            // Delete all region files and set for continue with loading schematics
            if (success) {
                for (WorldFilesProcess process : processes) {
                    process.deleteAllRegionsFile();
                }
                dataManager.setContinueLoading(true);
            }

        } catch (Exception e) {
            plugin.error("There was an error running the OrchestratorRegen 1st step", e);
        } finally {
            this.lock.unlock();
        }
        RestartServerRunnable restartServerRunnable = new RestartServerRunnable();
        restartServerRunnable.runTask(plugin);
    }

    private void loadSchematics() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        try {
            plugin.info("Starting loading schematics");
        } catch (Exception e) {
            plugin.error("There was an error running the OrchestratorRegen", e);
        } finally {
            this.lock.unlock();
        }
        UnRegisterEventRunnable unRegisterEventRunnable = new UnRegisterEventRunnable(listener);
        unRegisterEventRunnable.runTask(plugin);
    }

    private List<ChunkInFile> getChunksSave(World world, WorldFilesProcess worldFilesProcess) {
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        WorldRegenerationData data = dataManager.getDataOf(world);
        List<ChunkInFile> chunksForSave;
        if (data.getChunksToSaveSchematic().isEmpty()) {
            chunksForSave = worldFilesProcess.getProtectedChunks();
            dataManager.addChunksSaveSchematics(world, chunksForSave);
        } else {
            chunksForSave = data.getChunksToSaveSchematic();
        }
        return chunksForSave;
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
    private <T extends SynchronizeRunnable> List<T> splitChunks(Class<T> type, List<ChunkInFile> chunks, World world) {
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
                        ? new LoadSchematicRunnable(this)
                        : new SaveSchematicChunkRunnable(this, chunksSplit, world);
                executables.add((T) runnable);
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            SynchronizeRunnable runnable = type == LoadSchematicRunnable.class
                    ? new LoadSchematicRunnable(this)
                    : new SaveSchematicChunkRunnable(this, chunksSplit, world);
            executables.add((T) runnable);
        }
        return executables;
    }
}
