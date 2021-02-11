package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.RegenWorld;
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
     * First it going to delete the region file without claimed chunks using the class {@link RegenWorld}.
     * Then it going to create the temporal world, regen chunks, and delete temporal worlds
     *
     * @throws InterruptedException Throws if there is an error waiting other thread
     */
    private void executeTasks() throws InterruptedException {
        APlugin plugin = APlugin.getInstance();

        /*
         The tasks RegenWorldRunnable can run in the same thread because
         it doesn't access to the Bukkit API. Also in this chunk of
         code it calculates the next executables.
         */
        Queue<SynchronizeRunnable> executables = new LinkedList<>();
        for (World world : this.worldsToRegen) {
            RegenWorld worldRegen = new RegenWorld(world);
            worldRegen.run();
            List<SynchronizeRunnable> executablesForWorld = this.getWorldExecutables(world, worldRegen);
            executables.addAll(executablesForWorld);
        }

        long delay = plugin.getConfig().getLong("timeOfGraceForServer.chunkRegen");
        plugin.info("It is going to run {} regen task with {} tickets of delay", executables.size(), delay);

        for (SynchronizeRunnable task : executables) {
            task.runTaskLater(plugin, delay);
            this.condition.await();
        }
    }

    /**
     * Get the list of executables. This method also create the temporal world because
     * it is necessary for the other runnable
     *
     * @param world      world to regen
     * @param worldRegen Regenerate info
     * @return List of executables
     * @throws InterruptedException It will throw it if there is an error waiting the
     *                              temporal world creating
     */
    private List<SynchronizeRunnable> getWorldExecutables(World world, RegenWorld worldRegen)
            throws InterruptedException {
        APlugin plugin = APlugin.getInstance();
        List<SynchronizeRunnable> executables = new LinkedList<>();
        if (!worldRegen.getChunksToRegen().isEmpty()) {
            // Creates the temporal world
            CreateWorldRunnable createWorldRunnable = new CreateWorldRunnable(this, world);
            long delayWorld = plugin.getConfig().getLong("timeOfGraceForServer.worldCreating");
            createWorldRunnable.runTaskLater(plugin, delayWorld);
            this.condition.await();

            // Adds executables for chunks and delete world
            if (createWorldRunnable.getClone() != null) {
                List<ChunkInFile> chunksToRegen = this.filterChunksExecutables(
                        worldRegen.getChunksToRegen(), world, createWorldRunnable.getClone());
                for (ChunkInFile chunkToRegen : chunksToRegen) {
                    RegenChunkRunnable runnable = new RegenChunkRunnable(
                            this, world, createWorldRunnable.getClone(), chunkToRegen);
                    executables.add(runnable);
                }
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
    private List<ChunkInFile> filterChunksExecutables(List<ChunkInFile> chunksToRegen, World real,
                                                      World clone) throws InterruptedException {
        List<FilterChunksRunnable> executables = new LinkedList<>();
        ConcurrentLinkedDeque<ChunkInFile> chunksSplit = new ConcurrentLinkedDeque<>();
        int i = 0;
        int splitSize = APlugin.getInstance().getConfig().getInt("timeOfGraceForServer.filterChunkSize");
        for (ChunkInFile chunkInFile : chunksToRegen) {
            if (i < splitSize) {
                chunksSplit.add(chunkInFile);
                i++;
            } else {
                executables.add(new FilterChunksRunnable(this, chunksSplit, real, clone));
                chunksSplit = new ConcurrentLinkedDeque<>();
                i = 0;
            }
        }
        if (!chunksSplit.isEmpty()) {
            executables.add(new FilterChunksRunnable(this, chunksSplit, real, clone));
        }

        List<ChunkInFile> result = new LinkedList<>();
        int delayFilter = APlugin.getInstance().getConfig().getInt("timeOfGraceForServer.filterChunk");
        for (FilterChunksRunnable runnable : executables) {
            runnable.runTaskLater(APlugin.getInstance(), delayFilter);
            this.condition.await();
            result.addAll(runnable.getChunksToRegen());
        }
        return result;
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getCondition() {
        return condition;
    }
}
