package co.andrescol.mc.plugin.turtleresetworld.task.clean.objects;

/**
 * This class allow to locate the chunk in the region file
 */
public class ChunkInFile {

    private final int x;
    private final int z;
    private final boolean protectedChunk;
    private final int arrayPosition;

    public ChunkInFile(int x, int z, boolean protectedChunk) {
        this.x = x;
        this.z = z;
        this.protectedChunk = protectedChunk;
        this.arrayPosition = ((x & 31) + (z & 31) * 32);
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
        return String.format("%d/%d -> protected: %b , pos: %d",
                this.x, this.z, this.protectedChunk, this.arrayPosition);
    }
}
