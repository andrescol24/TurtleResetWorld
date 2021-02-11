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
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.FERN));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.SUGAR_CANE));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.CACTUS));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.BROWN_MUSHROOM));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.CAVE_AIR, Material.BROWN_MUSHROOM));

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
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.ACACIA_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.BIRCH_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.DARK_OAK_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.JUNGLE_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.OAK_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.AIR, Material.SPRUCE_LEAVES));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.CAVE_AIR, Material.WATER));
        IGNORABLE_REPLACEMENTS.add(new IgnoreBlockChange(Material.CAVE_AIR, Material.LAVA));

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
            boolean same = this.compareChunkAirBlocksAndWaterBlocksAndContainers(chunk, chunkClone);
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
    private boolean compareChunkAirBlocksAndWaterBlocksAndContainers(Chunk chunk, Chunk chunkClone) {
        int maxDifferentBlocks = APlugin.getInstance().getConfig().getInt("filterChunk.maxDifferentBlocksOfAir");
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block realBlock = chunk.getBlock(x, y, z);
                    Block cloneBlock = chunkClone.getBlock(x, y, z);
                    if (realBlock.getType() != cloneBlock.getType()) {
                        if (!IGNORABLE_REPLACEMENTS.contains
                                (new IgnoreBlockChange(cloneBlock.getType(), realBlock.getType()))) {
                            if(isAirOrWater(realBlock.getType()) || isAirOrWater(cloneBlock.getType())) {
                                APlugin.getInstance().info("different: {} - {}", cloneBlock.getType(),
                                        realBlock.getType());
                                count++;
                            }
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

    private boolean isAirOrWater(Material material) {
        switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case WATER:
                return true;
            default:
                return false;
        }
    }

    private boolean hasDifferentContent(Block cloneBlock, Block realBlock) {
        ItemStack[] newContainer = ((Container) cloneBlock.getState()).getInventory().getContents();
        ItemStack[] oldContainer = ((Container) realBlock.getState()).getInventory().getContents();
        for(int i = 0; i < newContainer.length && i < oldContainer.length; i++) {
            ItemStack item = newContainer[i];
            if(item != null && !item.equals(oldContainer[i]))  {
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
