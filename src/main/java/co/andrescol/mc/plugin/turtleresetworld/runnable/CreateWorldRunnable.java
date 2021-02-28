package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class CreateWorldRunnable extends SynchronizeRunnable {

    private final World real;
    private World clone;

    public CreateWorldRunnable(OrchestratorRunnable orchestrator, World realWorld) {
        super(orchestrator);
        this.real = realWorld;
    }

    @Override
    protected void execute() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("-------- Starting creating world for {} ------------", this.real.getName());
        WorldCreator creator = new WorldCreator("cloneOf_" + this.real.getName())
                .environment(this.real.getEnvironment()).seed(this.real.getSeed());
        this.clone = creator.createWorld();
        if (this.clone == null) {
            plugin.warn("The Clone world could not be created. Contact with the developer");
        }
    }

    @Override
    public long getDelay() {
        return 200L;
    }

    public World getClone() {
        return this.clone;
    }

    @Override
    public String toString() {
        return String.format("CreateWorldRunnable of %s", this.real.getName());
    }
}
