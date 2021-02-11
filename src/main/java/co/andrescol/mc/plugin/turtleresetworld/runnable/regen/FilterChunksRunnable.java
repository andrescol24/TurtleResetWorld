package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.ChunkInFile;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FilterChunksRunnable extends SynchronizeRunnable {

    private final ConcurrentLinkedDeque<ChunkInFile> chunksToRegen;
    private final World real;
    private final World clone;

    public FilterChunksRunnable(OrchestratorRegenRunnable orchestrator, ConcurrentLinkedDeque<ChunkInFile> chunksToRegen, World real, World clone) {
        super(orchestrator);
        this.chunksToRegen = chunksToRegen;
        this.real = real;
        this.clone = clone;
    }

    @Override
    protected void execute() {
        int size = this.chunksToRegen.size();
        for (ChunkInFile chunkInFile : this.chunksToRegen) {
            Chunk chunk = this.real.getChunkAt(chunkInFile.getX(), chunkInFile.getZ());
            Chunk chunkClone = clone.getChunkAt(chunkInFile.getX(), chunkInFile.getZ());
            boolean same = this.compareChunks(chunk, chunkClone);
            if (same) {
                boolean removed = this.chunksToRegen.remove(chunkInFile);
                if (removed) {

                }
            }
        }
        APlugin.getInstance().info("Filtered {} chunks", size - this.chunksToRegen.size());
    }

    private boolean compareChunks(Chunk chunk, Chunk chunkClone) {
        int maxDifferentBlocks = APlugin.getInstance().getConfig().getInt("filterChunk.maxDifferentBlocks");
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < this.real.getHighestBlockYAt(x, z); y++) {
                    Block realBlock = chunk.getBlock(x, y, z);
                    Block cloneBlock = chunkClone.getBlock(x, y, z);
                    if (realBlock.getType() != cloneBlock.getType()) {
                        count++;
                    } else {
                        APlugin.getInstance().info("different types: {} - {}", realBlock.getType(), cloneBlock.getType());
                    }
                    if (count > maxDifferentBlocks) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public ConcurrentLinkedDeque<ChunkInFile> getChunksToRegen() {
        return chunksToRegen;
    }

    @Override
    public String toString() {
        return String.format("FilterChunksRunnable of %s", this.real.getName());
    }
}
