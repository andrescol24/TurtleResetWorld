package co.andrescol.mc.plugin.turtleresetworld.task.clean;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RegenTask extends BukkitRunnable {

    private final List<World> worlds;

    /**
     * Creates an instance of this Runnable
     *
     * @param worlds List of worlds that going to be deleted
     */
    public RegenTask(List<World> worlds) {
        this.worlds = worlds;
    }

    @Override
    public void run() {
        for (World world : this.worlds) {
            RegenWorldTask cleanTask = new RegenWorldTask(world);
            cleanTask.run();
        }

        // Run in the main thread
        PostCleanRegionsTask post = new PostCleanRegionsTask();
        post.runTask(APlugin.getInstance());
    }
}
