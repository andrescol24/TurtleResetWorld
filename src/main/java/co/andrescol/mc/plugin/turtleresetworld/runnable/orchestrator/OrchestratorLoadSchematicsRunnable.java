package co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.listener.AntiPlayerJoinListener;
import co.andrescol.mc.plugin.turtleresetworld.runnable.regen.LoadSchematicRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.postregen.UnRegisterEventRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;

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
            for(LoadSchematicRunnable runnable : executables) {
                runnable.runTask(plugin);
                this.condition.await();
                dataManager.removeChunksLoadSchematics(runnable.getWorld(), runnable.getChunks());
                this.controlTimeoutOut();
            }
            dataManager.setContinueLoading(false);
        } catch (Exception e) {
            plugin.error("There was an error running the OrchestratorRegen", e);
        } finally {
            this.lock.unlock();
        }
        UnRegisterEventRunnable unRegisterEventRunnable = new UnRegisterEventRunnable(listener);
        unRegisterEventRunnable.runTask(plugin);
    }
}
