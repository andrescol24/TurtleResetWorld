package co.andrescol.mc.plugin.turtleresetworld.objects;

/**
 * This class allow to locate the chunk in the region file
 */
public class ChunkInFile {

    private final int x;
    private final int z;
    private final boolean protectedChunk;
    private final int arrayPosition;

    // File data
    private byte[] data;
    private int chunkSize;
    private byte compressionType;

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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public byte getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(byte compressionType) {
        this.compressionType = compressionType;
    }

    @Override
    public String toString() {
        return String.format("%d/%d -> protected: %b , pos: %d, size(B): %d",
                this.x, this.z, this.protectedChunk, this.arrayPosition, this.chunkSize);
    }
}
