package co.andrescol.mc.plugin.turtleresetworld.util;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import co.andrescol.mc.plugin.turtleresetworld.data.RegenerationDataManager;
import co.andrescol.mc.plugin.turtleresetworld.data.WorldRegenerationData;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This runnable is going to regen only the regions without claimed chunks.
 * The chunks that need to be regenerated can be accessed with the method
 * {@link WorldFilesProcess#getChunksToRegen()}
 */
public class WorldFilesProcess {

    private final World world;
    private List<ChunkInFile> chunksToRegen = new LinkedList<>();

    public WorldFilesProcess(World worldToRegen) {
        this.world = worldToRegen;
    }

    /**
     * Read all region files and it determinate if there are
     * claimed chunks.
     */
    public void run(boolean deleteRegionsNotClaimed) {
        APlugin plugin = APlugin.getInstance();
        try {
            RegenerationDataManager dataManager = RegenerationDataManager.getInstance();
            WorldRegenerationData data = dataManager.getDataOf(this.world);
            List<ChunkInFile> listChunks = data.getChunksToRegen();
            if (!listChunks.isEmpty()) {
                plugin.info("Continue with {} chunks in the data", listChunks.size());
                this.chunksToRegen = listChunks;
            } else {
                APlugin.getInstance().info("Last regeneration date {}", new Date(data.getLastRegeneration()));
                List<RegionInFile> regions = this.getRegionsInWorldFolder(data.getLastRegeneration());
                for (RegionInFile region : regions) {
                    if (region.hasClaimedChunks()) {
                        this.chunksToRegen.addAll(region.getUnclaimedChunks());
                    } else if (deleteRegionsNotClaimed) {
                        boolean deleted = region.deleteFile();
                        if (!deleted) {
                            plugin.warn("{} couldn't be deleted", region);
                        } else {
                            plugin.info("{} deleted!", region);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.error("Error during the world {} regen", e, world.getName());
        }
    }

    public List<ChunkInFile> getChunksToRegen() {
        return chunksToRegen;
    }

    /**
     * Get the list of loaded regions in the world reading the region folder
     *
     * @param lastRegeneration Last regeneration in seconds
     * @return List of regions
     * @throws IOException Throws it if there is an error reading the region file
     */
    private List<RegionInFile> getRegionsInWorldFolder(long lastRegeneration) throws IOException {
        List<RegionInFile> regions = new LinkedList<>();
        File regionFolder = this.getRegionFolder();
        if (regionFolder.exists()) {
            File[] files = Objects.requireNonNull(regionFolder.listFiles());
            List<Chunk> claimedChunks = this.getClaimedChunks();
            for (File file : files) {
                RegionInFile region = new RegionInFile(file, claimedChunks, lastRegeneration);
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
}
