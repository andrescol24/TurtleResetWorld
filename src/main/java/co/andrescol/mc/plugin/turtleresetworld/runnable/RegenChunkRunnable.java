package co.andrescol.mc.plugin.turtleresetworld.runnable;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.task.clean.objects.ChunkInFile;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

/**
 * This Runnable access to Bukkit API to copy blocks from the clone world to
 * the real world.
 */
public class RegenChunkRunnable extends SynchronizeRunnable {

    private final World real;
    private final World clone;
    private final int x;
    private final int z;

    public RegenChunkRunnable(OrchestratorRegenRunnable orchestrator, World real,
                              World clone, ChunkInFile chunk) {
        super(orchestrator);
        this.real = real;
        this.clone = clone;
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    @Override
    protected void execute() {
        // Load Chunks
        this.real.loadChunk(this.x, this.z);
        clone.loadChunk(this.x, this.z);

        Chunk chunk = this.real.getChunkAt(this.x, this.z);
        Chunk chunkClone = clone.getChunkAt(this.x, this.z);
        this.copyBlock(chunkClone, chunk);

        // Unload Chunks
        this.real.unloadChunk(chunk);
        clone.unloadChunk(chunkClone);
        APlugin.getInstance().info(
                "Blocks for chunks {}/{} in world {} copied!", this.x, this.z, this.real);
    }

    private void copyBlock(Chunk from, Chunk to) {
        for (int y = 0; y < this.real.getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = from.getBlock(x, y, z);

                    // Coping block data
                    BlockData data = block.getBlockData();
                    Block newBlock = to.getBlock(x, y, z);
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

        for (Entity entity : from.getEntities()) {
            Location entityLocation = entity.getLocation();
            entityLocation.setWorld(clone);
            entity.teleport(entityLocation);
        }
    }
}
