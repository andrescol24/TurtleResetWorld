package co.andrescol.mc.plugin.turtleresetworld.runnable.copy;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CopyChunkRunnable extends SynchronizeRunnable {

    private final World to;
    private final World from;
    private final ConcurrentLinkedDeque<ChunkInFile> chunks;

    public CopyChunkRunnable(OrchestratorRunnable orchestrator, World to,
                              World from, ConcurrentLinkedDeque<ChunkInFile> chunks) {
        super(orchestrator);
        this.to = to;
        this.from = from;
        this.chunks = chunks;
    }

    public World getFrom() {
        return from;
    }

    public ConcurrentLinkedDeque<ChunkInFile> getChunks() {
        return chunks;
    }

    @Override
    protected void execute() {
        for (ChunkInFile chunkFile : this.chunks) {
            long start = System.currentTimeMillis();
            Chunk chunk = this.to.getChunkAt(chunkFile.getX(), chunkFile.getZ());
            Chunk chunkClone = from.getChunkAt(chunkFile.getX(), chunkFile.getZ());
            this.copyChunk(chunkClone, chunk);
            this.to.unloadChunk(chunk);
            from.unloadChunk(chunkClone);
            long end = System.currentTimeMillis() - start;
            APlugin.getInstance().info("Copy from {} to {} chunk {}/{} time: {}ms",
                    from.getName(), to.getName(), chunkFile.getX(), chunk.getZ(), end);
        }
        this.orchestrator.setTotalChunks(this.orchestrator.getTotalChunks() - this.chunks.size());
        APlugin.getInstance().info("{} chunks left", this.orchestrator.getTotalChunks());
    }

    @Override
    public long getDelay() {
        return 100L;
    }

    private void copyChunk(Chunk from, Chunk to) {
        List<Block[]> rightChests = new LinkedList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block block = from.getBlock(x, y, z);
                    Block newBlock = to.getBlock(x, y, z);
                    this.copyBlock(block, newBlock, rightChests);
                }
            }
        }
        // Inventory of double chest
        for (Block[] chest : rightChests) {
            Container oldContainer = (Container) chest[0].getState();
            Container newContainer = (Container) chest[1].getState();
            newContainer.getInventory().setContents(oldContainer.getInventory().getContents());
        }
        boolean includeEntities = APlugin.getInstance().getConfig().getBoolean("includeEntities");
        if (includeEntities) {
            for (Entity entity : from.getEntities()) {
                Location entityLocation = entity.getLocation();
                entityLocation.setWorld(this.from);
                entity.teleport(entityLocation);
            }
        }
    }

    /**
     * This method copy the block information, type and content from the block to
     * the newBlock
     *
     * @param block       Block with the information
     * @param newBlock    Block that will be replaced
     * @param rightChests a tuple of block and newBlock if the block is a right chest.
     *                    That mean that the inventory have to be copied then. If the block is
     *                    right chest it will add the block and newBlock to the list
     */
    private void copyBlock(Block block, Block newBlock, List<Block[]> rightChests) {
        try {
            if (block.getBlockData() instanceof Bisected) { // Doors
                newBlock.setType(block.getType(), false);
            } else if (block.getBlockData() instanceof Bed) { // Beds
                newBlock.setType(block.getType(), false);
            } else { // Others
                newBlock.setType(block.getType());
            }
            newBlock.setBlockData(block.getBlockData());
            newBlock.getState().setData(block.getState().getData().clone());

            BlockState state = block.getState();
            if (block.getBlockData() instanceof Chest) { // chest
                Chest oldChest = (Chest) block.getBlockData();
                switch (oldChest.getType()) {
                    case SINGLE:
                        Container newContainer = (Container) newBlock.getState();
                        Container oldContainer = (Container) state;
                        newContainer.getInventory().setContents(oldContainer.getInventory().getContents());
                        break;
                    case LEFT: // First set de blocks and then copy the inventory
                        break;
                    case RIGHT:
                        Block[] chest = new Block[2];
                        chest[0] = block;
                        chest[1] = newBlock;
                        rightChests.add(chest);
                        break;
                }
            } else if (state instanceof InventoryHolder) { // Other containers
                InventoryHolder newContainer = (InventoryHolder) newBlock.getState();
                InventoryHolder oldContainer = (InventoryHolder) state;
                newContainer.getInventory().setContents(oldContainer.getInventory().getContents());
            }
        } catch (Exception e) {
            APlugin.getInstance().error("The block {} located in: {} can not be to copy", e, block.getType(),
                    block.getLocation());
        }
    }

    @Override
    public String toString() {
        return String.format("RegenChunkRunnable of %s", this.to.getName());
    }
}
