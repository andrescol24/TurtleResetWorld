package co.andrescol.mc.plugin.turtleresetworld.objects;

import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import org.bukkit.Chunk;

import java.io.File;
import java.util.List;

/**
 * This class facility the region process.
 * Read https://minecraft.gamepedia.com/Region_file_format for more information
 */
public class Region {

    private final int x;
    private final int z;
    private File file;

    /**
     * Create an instance of this class
     *
     * @param regionFile Region file
     */
    public Region(File regionFile) {
        if (regionFile.getName().endsWith(".mca")) {
            String[] split = regionFile.getName().split("\\.");
            if (split.length == 4) {
                this.x = Integer.parseInt(split[1]);
                this.z = Integer.parseInt(split[2]);
                this.file = regionFile;
            } else {
                throw new IllegalStateException("The file " + file.getName() + "isn't a region file");
            }
        } else {
            throw new IllegalStateException("The file " + file.getName() + "isn't a region file");
        }
    }

    /**
     * Determinate if the region file contains chunks claimed
     *
     * @param claimers claimers
     * @return <code>true</code> if the claimers has claimed chunks
     */
    public boolean hasClaimedChunks(List<Claimer> claimers) {
        for(Claimer claimer : claimers) {
            for(Chunk chunk : claimer.getClaimedChunks()) {
                int xCalculated = Math.floorDiv(chunk.getX(), 32);
                int zCalculated = Math.floorDiv(chunk.getZ(), 32);
                if(this.x == xCalculated && this.z == zCalculated) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes the region file
     * @return true if the region file was deleted
     */
    public boolean deleteFile() {
        return this.file.delete();
    }

    @Override
    public String toString() {
        return String.format("x: %d, z: %d, file: %s", this.x, this.z, this.file.getName());
    }
}
