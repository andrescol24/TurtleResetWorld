package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This Runnable access to Bukkit API to copy blocks from the clone world to
 * the real world.
 */
public class RegenChunkRunnable extends SynchronizeRunnable {

    private final World real;
    private final World clone;
    private final ConcurrentLinkedDeque<ChunkInFile> chunks;

    public RegenChunkRunnable(OrchestratorRegenRunnable orchestrator, World real,
                              World clone, ConcurrentLinkedDeque<ChunkInFile> chunks) {
        super(orchestrator);
        this.real = real;
        this.clone = clone;
        this.chunks = chunks;
    }

    @Override
    protected void execute() {
        for(ChunkInFile chunkFile : this.chunks) {
            Chunk chunk = this.real.getChunkAt(chunkFile.getX(), chunkFile.getZ());
            Chunk chunkClone = clone.getChunkAt(chunkFile.getX(), chunkFile.getZ());
            this.copyBlock(chunkClone, chunk);
            this.real.unloadChunk(chunk);
            clone.unloadChunk(chunkClone);
        }
        this.orchestrator.setTotalChunks(this.orchestrator.getTotalChunks() - this.chunks.size());
        APlugin.getInstance().info("{} chunks left", this.orchestrator.getTotalChunks());
    }

    private void copyBlock(Chunk from, Chunk to) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block block = from.getBlock(x, y, z);
                    Block newBlock = to.getBlock(x, y, z);

                    newBlock.setType(block.getType());
                    BlockData data = block.getBlockData();
                    newBlock.setBlockData(data);

                    // Coping block state
                    BlockState state = block.getState();
                    newBlock.getState().setData(state.getData());

                    if (state instanceof Container) {
                        Container newContainer = (Container) newBlock.getState();
                        Container oldContainer = (Container) state;
                        newContainer.getInventory().setContents(oldContainer.getInventory().getContents());
                        newContainer.setCustomName(oldContainer.getCustomName());
                    }

                    if (state instanceof Nameable) {
                        Nameable newNameable = (Nameable) newBlock.getState();
                        Nameable oldNameable = (Nameable) state;
                        newNameable.setCustomName(oldNameable.getCustomName());
                    }
                }
            }
        }
        boolean includeEntities = APlugin.getInstance().getConfig().getBoolean("includeEntities");
        if(includeEntities) {
            for (Entity entity : from.getEntities()) {
                Location entityLocation = entity.getLocation();
                entityLocation.setWorld(clone);
                entity.teleport(entityLocation);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("RegenChunkRunnable of %s", this.real.getName());
    }
}
