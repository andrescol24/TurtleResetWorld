package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SaveSchematicChunkRunnable extends SynchronizeRunnable {

    private final ConcurrentLinkedDeque<ChunkInFile> chunks;
    private final World world;

    public SaveSchematicChunkRunnable(OrchestratorRunnable orchestrator,
                                      ConcurrentLinkedDeque<ChunkInFile> chunks, World world) {
        super(orchestrator);
        this.chunks = chunks;
        this.world = world;
    }

    @Override
    protected void execute() {
        for(ChunkInFile chunk : this.chunks) {

        }
    }

    @Override
    public long getDelay() {
        return 100;
    }
}
