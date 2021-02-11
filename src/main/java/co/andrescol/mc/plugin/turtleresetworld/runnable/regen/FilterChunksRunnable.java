package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.runnable.util.IgnoreBlockChange;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FilterChunksRunnable extends SynchronizeRunnable {

    private final ConcurrentLinkedDeque<ChunkInFile> chunksToRegen;
    private final World real;
    private final World clone;

    /**
     * Contains tuples of [CloneMaterial, RealMaterial].
     * Only contains blocks that can grow like grass or flowers
     */
    private static final Set<IgnoreBlockChange> IGNORABLE_REPLACEMENTS = new HashSet<>();

    static {
        // Plants that ground in land
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.LARGE_FERN));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.TALL_GRASS));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.GRASS));

        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.DANDELION));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.POPPY));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.BLUE_ORCHID));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.ALLIUM));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.AZURE_BLUET));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.ORANGE_TULIP));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.PINK_TULIP));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.RED_TULIP));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.WHITE_TULIP));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.OXEYE_DAISY));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.CORNFLOWER));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.LILY_OF_THE_VALLEY));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.SUNFLOWER));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.LILAC));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.ROSE_BUSH));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.PEONY));

        // Plants that ground in water
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.WATER, Material.TALL_SEAGRASS));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.WATER, Material.KELP_PLANT));

    }

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
                this.chunksToRegen.remove(chunkInFile);
            }
        }
        APlugin.getInstance().info("Filtered {} chunks of {}", size - this.chunksToRegen.size(), size);
    }

    /**
     * Compares the chunk.
     *
     * @param chunk      Real
     * @param chunkClone Clone
     * @return true if the same
     */
    private boolean compareChunks(Chunk chunk, Chunk chunkClone) {
        int maxDifferentBlocks = APlugin.getInstance().getConfig().getInt("filterChunk.maxDifferentBlocks");
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block realBlock = chunk.getBlock(x, y, z);
                    Block cloneBlock = chunkClone.getBlock(x, y, z);
                    if (realBlock.getType() != cloneBlock.getType()) {
                        if (!IGNORABLE_REPLACEMENTS.contains
                                (new IgnoreBlockChange(cloneBlock.getType(), realBlock.getType()))) {
                            APlugin.getInstance().info("different: {} - {}", cloneBlock.getType(), realBlock.getType());
                            count++;
                        }
                    } else if(cloneBlock.getState() instanceof Container) {
                        if(this.hasDifferentContent(cloneBlock, realBlock)) {
                            APlugin.getInstance().info("different content in {}", realBlock.getLocation());
                            return true;
                        }
                    }
                    if (count > maxDifferentBlocks) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDifferentContent(Block cloneBlock, Block realBlock) {
        ItemStack[] newContainer = ((Container) cloneBlock.getState()).getInventory().getContents();
        ItemStack[] oldContainer = ((Container) realBlock.getState()).getInventory().getContents();
        for(int i = 0; i < oldContainer.length; i++) {
            if(!newContainer[i].equals(oldContainer[i]))  {
                return true;
            }
        }
        return false;
    }

    public ConcurrentLinkedDeque<ChunkInFile> getChunksToRegen() {
        return chunksToRegen;
    }

    @Override
    public String toString() {
        return String.format("FilterChunksRunnable of %s", this.real.getName());
    }
}
