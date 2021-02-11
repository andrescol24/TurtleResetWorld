package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.task.clean.objects.ChunkInFile;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
     * First it going to delete the region file without claimed chunks using the class {@link RegenWorld}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     *
     * @throws InterruptedException
     */
    private void executeTasks() throws InterruptedException {
        APlugin plugin = APlugin.getInstance();

        /*
         The tasks RegenWorldRunnable can run in the same thread because
         it doesn't access to the Bukkit API. Also in this chunk of
         code it calculates the next runnables.
         */
        Queue<SynchronizeRunnable> runnables = new LinkedList<>();
        for (World world : this.worldsToRegen) {
            RegenWorld worldRegen = new RegenWorld(world);
            worldRegen.run();
            if (!worldRegen.getChunksToRegen().isEmpty()) {
                // Creates the temporal world
                CreateWorldRunnable createWorldRunnable = new CreateWorldRunnable(this, world);
                createWorldRunnable.runTaskLater(plugin, 1000);
                this.condition.await();

                // Adds runnables for chunks and delete world
                if (createWorldRunnable.getClone() != null) {
                    for (ChunkInFile chunkToRegen : worldRegen.getChunksToRegen()) {
                        RegenChunkRunnable runnable = new RegenChunkRunnable(
                                this, world, createWorldRunnable.getClone(), chunkToRegen);
                        runnables.add(runnable);
                    }
                    DeleteWorldRunnable deleteRunnable = new DeleteWorldRunnable(
                            this, createWorldRunnable.getClone());
                    runnables.add(deleteRunnable);
                }
            }
        }

        long delay = plugin.getConfig().getLong("ticksBetweenChunkRegen");
        plugin.info("It is going to run {} regen task with {} tickets of delay", runnables.size(), delay);

        for (SynchronizeRunnable task : runnables) {
            task.runTaskLater(plugin, delay);
            this.condition.await();
        }
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getCondition() {
        return condition;
    }
}
