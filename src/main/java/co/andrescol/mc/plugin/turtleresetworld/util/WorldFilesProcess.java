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

public class WorldFilesProcess {

    private List<RegionInFile> regions;
    private String worldName;

    /**
     * create an instance of the world
     *
     * @param worldToRegen World that will be regenerated
     */
    public WorldFilesProcess(World worldToRegen) {
        APlugin plugin = APlugin.getInstance();
        try {
            this.worldName = worldToRegen.getName();
            this.regions = this.getRegionsInWorldFolder(worldToRegen);
        } catch (Exception e) {
            this.regions = new LinkedList<>();
            plugin.error("Error during the world {} regen", e, worldToRegen.getName());
        }
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

    /**
     * Move all region files to the backup/world/ plugin directory
     */
    public void moveAllRegionsFile() {
        APlugin plugin = APlugin.getInstance();
        plugin.info("Moving {} region files", this.worldName);
        for (RegionInFile region : this.regions) {
            boolean moved = region.moveFile(this.worldName);
            if (moved) {
                plugin.info("{} file was moved", region);
            } else {
                plugin.warn("{} file was not moved", region);
            }
        }
    }

    public void deleteBackupFolder() {
        String path = "backup" + File.separator + worldName;
        File folder = new File(APlugin.getInstance().getDataFolder(),path);
        if(folder.exists()) {
            APlugin.getInstance().info("Deleting {} folder", path);
            for(File file : Objects.requireNonNull(folder.listFiles())) {
                file.delete();
            }
            folder.delete();
        }
    }

    /**
     * Get the list of loaded regions in the world reading the region folder
     *
     * @param world World to process
     * @return List of regions
     * @throws IOException Throws it if there is an error reading the region file
     */
    private List<RegionInFile> getRegionsInWorldFolder(World world) throws IOException {
        List<RegionInFile> regions = new LinkedList<>();
        File regionFolder = this.getRegionFolder(world);
        if (regionFolder.exists()) {
            File[] files = Objects.requireNonNull(regionFolder.listFiles());
            List<Chunk> claimedChunks = TurtleResetWorldPlugin.getChunksClaimedByHooks()
                    .stream()
                    .filter(chunk -> chunk.getWorld().equals(world))
                    .collect(Collectors.toList());
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
     * @param world World to process
     * @return The region folder
     */
    private File getRegionFolder(World world) {
        File worldDirectory = world.getWorldFolder();
        String regionPath;
        switch (world.getEnvironment()) {
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
}
