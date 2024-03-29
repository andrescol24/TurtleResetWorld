package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.RestartServerRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.SaveSchematicChunkRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.util.WorldFilesProcess;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;

public class OrchestratorSaveSchematicsRunnable extends OrchestratorRunnable {

    private final List<World> worldsToRegen;

    /**
     * Create an instance of this orchestrator
     *
     * @param worldsToRegen Worlds to regen
     */
    public OrchestratorSaveSchematicsRunnable(List<World> worldsToRegen) {
        this.worldsToRegen = worldsToRegen;
        this.totalChunks = 0;
    }

    @Override
    public void run() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
        try {
            // List all process
            List<WorldFilesProcess> processes = new LinkedList<>();
            List<SaveSchematicChunkRunnable> executables = new LinkedList<>();
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
                this.totalChunks += chunksForSave.size();
                List<SaveSchematicChunkRunnable> savesExecutables = this.splitChunks
                        (SaveSchematicChunkRunnable.class, chunksForSave, world);
                executables.addAll(savesExecutables);
                processes.add(worldProcess);
            }

            // Run the executables for save the schematics of each protected chunk
            boolean success = true;
            plugin.info("Starting process to save the schematics of {} chunks", this.totalChunks);
            this.calculateTimeouts();
            for (SaveSchematicChunkRunnable runnable : executables) {
                runnable.runTask(plugin);
                this.condition.await();
                if (runnable.isSuccess()) {
                    dataManager.removeChunksSaveSchematics(runnable.getWorld(), runnable.getChunks());
                    dataManager.addChunksLoadSchematics(runnable.getWorld(), runnable.getChunks());
                    this.controlTimeoutOut();
                } else {
                    success = false;
                    break;
                }
            }

            // Delete all region files and set for continue with loading schematics
            if (success) {
                processes.forEach(WorldFilesProcess::deleteBackupFolder);
                if(plugin.getConfig().getBoolean("enableBackup")) {
                    processes.forEach(WorldFilesProcess::moveAllRegionsFile);
                } else {
                    processes.forEach(WorldFilesProcess::deleteRegionsFile);
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
}
