package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.RestartServerRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.UnRegisterEventRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.LoadSchematicRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class OrchestratorLoadSchematicsRunnable extends OrchestratorRunnable {

    private final AntiPlayerJoinListener listener;

    /**
     * Create an instance of this orchestrator
     *
     * @param listener      AntiPlayerJoinListener that going to be unregister after the regeneration
     */
    public OrchestratorLoadSchematicsRunnable(AntiPlayerJoinListener listener) {
        super();
        this.listener = listener;
        this.totalChunks = 0;
    }

    @Override
    public void run() {
        this.lock.lock();
        APlugin plugin = APlugin.getInstance();
        boolean success = true;
        try {
            RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
            List<World> worldsToRegen = dataManager.getListWorldsPending();
            List<LoadSchematicRunnable> executables = new LinkedList<>();
            for(World world: worldsToRegen) {
                List<ChunkInFile> chunksForLoad = dataManager.getDataOf(world).getChunksToLoadSchematic();
                List<LoadSchematicRunnable> loads = this.splitChunks(LoadSchematicRunnable.class, chunksForLoad, world);
                this.totalChunks += chunksForLoad.size();
                executables.addAll(loads);
            }

            plugin.info("Starting process to load the schematics of {} chunks", this.totalChunks);
            this.calculateTimeouts();
            for (LoadSchematicRunnable runnable : executables) {
                runnable.runTask(plugin);
                this.condition.await();
                if(runnable.isSuccess()) {
                    dataManager.removeChunksLoadSchematics(runnable.getWorld(), runnable.getChunks());
                    this.controlTimeoutOut();
                } else {
                    success = false;
                    break;
                }
            }
            if(success) {
                dataManager.setContinueLoading(false);
                File schematicsFolder = new File(plugin.getDataFolder(), "schematics");
                this.deleteFolder(schematicsFolder);
                dataManager.clearTeleportedPlayerList();
                UnRegisterEventRunnable unRegisterEventRunnable = new UnRegisterEventRunnable(listener);
                unRegisterEventRunnable.runTask(plugin);
            }
        } catch (Exception e) {
            plugin.error("There was an error running the OrchestratorRegen", e);
        } finally {
            this.lock.unlock();
        }
        if(!success) {
            plugin.info("There was an error regenerating some chunks, restarting...");
            RestartServerRunnable restartServerRunnable = new RestartServerRunnable();
            restartServerRunnable.runTask(plugin);
        }
    }

    /**
     * Delete a folder recursively
     *
     * @param folder folder
     */
    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    this.deleteFolder(file);
                }
            }
        }
        boolean deleted = folder.delete();
        if (!deleted) {
            APlugin.getInstance().info("The file {} could not be deleted", folder.getAbsolutePath());
        }
    }
}
