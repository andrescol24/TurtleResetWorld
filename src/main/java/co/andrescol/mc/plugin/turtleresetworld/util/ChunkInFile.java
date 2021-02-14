package co.andrescol.mc.plugin.turtleresetworld.util;

import java.util.Date;
import java.util.Objects;

/**
 * This class allow to locate the chunk in the region file
 */
public class ChunkInFile {

    private final int x;
    private final int z;
    private final boolean protectedChunk;
    private final int arrayPosition;
    private final Date date;

    public ChunkInFile(int x, int z, boolean protectedChunk, int[] timestamp) {
        this.x = x;
        this.z = z;
        this.protectedChunk = protectedChunk;
        this.arrayPosition = ((x & 31) + (z & 31) * 32);
        this.date = new Date((long)timestamp[this.arrayPosition] * 1000);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getArrayPosition() {
        return arrayPosition;
    }

    public boolean isProtectedChunk() {
        return protectedChunk;
    }

    @Override
    public String toString() {
        return String.format("[%d/%d]: protected: %b, last modification: %s",
                this.x, this.z, this.protectedChunk, this.date.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkInFile that = (ChunkInFile) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
