package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import org.bukkit.World;

import java.io.File;

public class DeleteWorldRunnable extends SynchronizeRunnable {

    private final World clone;

    public DeleteWorldRunnable(OrchestratorRunnable orchestrator, World clone) {
        super(orchestrator);
        this.clone = clone;
    }

    @Override
    protected void execute() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("--------- Starting delete of world {} ---------", this.clone.getName());
        plugin.getServer().unloadWorld(this.clone, false);
        boolean deleted = this.deleteDirectory(this.clone.getWorldFolder());
        if(deleted) {
            plugin.info("The folder of world {} was deleted", this.clone.getName());
        } else {
            plugin.warn("The folder of world {} was not deleted, please check it", this.clone.getName());
        }
    }

    @Override
    public long getDelay() {
        return 200L;
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Override
    public String toString() {
        return String.format("DeleteWorldRunnable of %s", this.clone.getName());
    }
}
