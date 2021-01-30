package co.andrescol.mc.plugin.turtleresetworld.objects;

import org.bukkit.Chunk;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class allow to locate the chunk in the region file
 */
public class ChunkRegion {

    private final int x;
    private final int z;
    private final int regionLocation;

    public ChunkRegion(int x, int z) {
        this.x = x;
        this.z = z;
        this.regionLocation = x + z * 32;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getRegionLocation() {
        return regionLocation;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof ChunkRegion) {
            ChunkRegion other = (ChunkRegion) object;
            return this.x == other.x && this.z == other.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.regionLocation;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d,location: %d]",this.x, this.z, this.regionLocation);
    }
}
