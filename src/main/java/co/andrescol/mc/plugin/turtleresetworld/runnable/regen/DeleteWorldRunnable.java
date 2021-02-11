package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.World;

public class DeleteWorldRunnable extends SynchronizeRunnable{

    private final World clone;

    public DeleteWorldRunnable(OrchestratorRegenRunnable orchestrator, World clone) {
        super(orchestrator);
        this.clone = clone;
    }

    @Override
    protected void execute() {
        APlugin.getInstance().info("Deleting world {}", this.clone);
    }

    @Override
    public String toString() {
        return String.format("DeleteWorldRunnable of %s", this.clone.getName());
    }
}