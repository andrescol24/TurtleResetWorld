package co.andrescol.mc.plugin.turtleresetworld.util;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.TurtleResetWorldPlugin;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
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
    private final List<RegionInFile> regions;

    /**
     * create an instance of the world
     *
     * @param worldToRegen World that will be regenerated
     */
    public WorldFilesProcess(World worldToRegen) {
        this.world = worldToRegen;
        this.regions = new LinkedList<>();
        APlugin plugin = APlugin.getInstance();
        try {
            this.regions.addAll(this.getRegionsInWorldFolder());
        } catch (Exception e) {
            plugin.error("Error during the world {} regen", e, world.getName());
        }
    }

    /**
     * Get an immutable list of the chunks to regen
     *
     * @return List with the unclaimed chunks
     */
    public List<ChunkInFile> getChunksToRegen() {
        List<ChunkInFile> chunks = new LinkedList<>();
        for (RegionInFile region : this.regions) {
            chunks.addAll(region.getUnclaimedChunks());
        }
        return chunks;
    }

    /**
     * Get an immutable list of protected chunks
     *
     * @return List with the claimed chunks
     */
    public List<ChunkInFile> getProtectedChunks() {
        List<ChunkInFile> chunks = new LinkedList<>();
        for (RegionInFile region : this.regions) {
            chunks.addAll(region.getClaimedChunks());
        }
        return chunks;
    }

    public void deleteRegionsUnclaimed() {
        APlugin plugin = APlugin.getInstance();
        for (RegionInFile region : this.regions) {
            if (!region.hasClaimedChunks()) {
                boolean deleted = region.deleteFile();
                if (deleted) {
                    plugin.info("{} file was deleted");
                } else {
                    plugin.warn("{} file was not deleted");
                }
            }
        }
    }

    /**
     * Get the list of loaded regions in the world reading the region folder
     *
     * @return List of regions
     * @throws IOException Throws it if there is an error reading the region file
     */
    private List<RegionInFile> getRegionsInWorldFolder() throws IOException {
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
}
