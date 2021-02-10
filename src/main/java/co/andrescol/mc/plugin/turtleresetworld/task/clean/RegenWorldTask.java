package co.andrescol.mc.plugin.turtleresetworld.task.clean;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.task.clean.objects.ChunkInFile;
import co.andrescol.mc.plugin.turtleresetworld.task.clean.objects.RegionInFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class allow to run the clean world task. This task will
 * create a temporal world and copy no-protected blocks and
 * entities to the real world
 */
public class RegenWorldTask extends BukkitRunnable {

    private final World world;
    private World clone;

    /**
     * Create an instance of this task with the world to regen
     *
     * @param world Worlds to regen
     */
    public RegenWorldTask(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("-------- Running world {} clean task --------", this.world.getName());
        try {
            List<RegionInFile> regions = this.getRegionsInFiles();
            for(RegionInFile region : regions) {
                if(region.hasClaimedChunks()) {
                    plugin.warn("The region file {} has claimed chunks", region);
                    this.copyBlocksNotClaimed(region);
                } else {
                    boolean deleted = region.deleteFile();
                    if (!deleted) {
                        plugin.warn("The region file {} couldn't be deleted", region);
                    } else {
                        plugin.info("Region {} deleted!", region);
                    }
                }
            }
        } catch (Exception e) {
            plugin.error("Error during the world {} regen", e, world.getName());
        }

    }

    private void copyBlocksNotClaimed(RegionInFile region) {
        World clone = this.getClone();
        for(ChunkInFile chunkFile : region.getChunksInFile()) {
            if(!chunkFile.isProtectedChunk()) {
                APlugin.getInstance().info("Coping blocks for chunk {}", chunkFile);
                // Load Chunks
                this.world.loadChunk(chunkFile.getX(), chunkFile.getZ());
                clone.loadChunk(chunkFile.getX(), chunkFile.getZ());

                Chunk chunk = this.world.getChunkAt(chunkFile.getX(), chunkFile.getZ());
                Chunk chunkClone = clone.getChunkAt(chunkFile.getX(), chunkFile.getZ());
                this.copyBlock(chunkClone, chunk);

                // Unload Chunks
                this.world.unloadChunk(chunk);
                clone.unloadChunk(chunkClone);
            }
        }
    }

    private void copyBlock(Chunk from, Chunk to) {
        for (int y = 0; y < 255; y++) {
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

    /**
     * Get the list of loaded regions in the world reading the region folder
     *
     * @return List of regions
     * @throws IOException Throws it if there is an error reading the region file
     */
    private List<RegionInFile> getRegionsInFiles() throws IOException {
        List<RegionInFile> regions = new LinkedList<>();
        File regionFolder = this.getRegionFolder();
        if (regionFolder.exists()) {
            File[] files = Objects.requireNonNull(regionFolder.listFiles());
            List<Chunk> claimedChunks = this.getClaimedChunks();
            for (File file : files) {
                RegionInFile region = new RegionInFile(file, claimedChunks);
                regions.add(region);
            }
            return regions;
        } else {
            throw new IllegalStateException("the region file "
                    + regionFolder.getAbsolutePath() + " doesn't exist");
        }
    }

    /**
     * Gets the region folder depending on the World's Environment
     *
     * @return The region folder
     */
    private File getRegionFolder() {
        File worldDirectory = this.world.getWorldFolder();
        String regionPath;
        switch (this.world.getEnvironment()) {
            case NETHER:
                regionPath = "DIM-1" + File.separator + "region";
                break;
            case THE_END:
                regionPath = "DIM1" + File.separator + "region";
                break;
            default:
                regionPath = "region";
        }
        return new File(worldDirectory, regionPath);
    }

    /**
     * Gets the list of the claimed chunks in the world
     *
     * @return List of claimed chunks
     */
    private List<Chunk> getClaimedChunks() {
        List<Chunk> chunks = TurtleResetWorldPlugin.getChunksClaimedByHooks();
        return chunks.stream()
                .filter(chunk -> chunk.getWorld().equals(this.world)).collect(Collectors.toList());
    }

    /**
     * Create a temporal world once to copy blocks and entities from it
     *
     * @return The temporal world
     */
    private World getClone() {
        if(this.clone == null) {
            WorldCreator creator = new WorldCreator("cloneOf_" + world.getName())
                    .environment(world.getEnvironment()).seed(world.getSeed());
            this.clone = creator.createWorld();
        }
        return this.clone;
    }
}
