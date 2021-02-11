package co.andrescol.mc.plugin.turtleresetworld.runnable.util;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Chunk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class facility the region file process.
 * Read https://minecraft.gamepedia.com/Region_file_format for more information
 */
public class RegionInFile {

    private final int x;
    private final int z;
    private final File file;
    private final List<ChunkInFile> chunksInFile;

    /**
     * offsets in 4KiB sector. ex:
     * location = 2 -> chunk starts in 8192
     */
    private static final int SECTOR_SIZE = 4096;

    /**
     * First part of the region file: 0 - 4095 bytes are the
     * location of the chunk in the file. Having 3 bytes of the offset
     * and 1 byte is the size of the chunk
     */
    private final int[] locations = new int[1024];

    /**
     * Create an instance of this class
     *
     * @param regionFile Region file
     * @throws IOException If and error occurs during the file reading
     */
    public RegionInFile(File regionFile, List<Chunk> protectedChunksWorld) throws IOException {
        if (regionFile.getName().endsWith(".mca")) {
            String[] split = regionFile.getName().split("\\.");
            if (split.length == 4) {
                this.x = Integer.parseInt(split[1]);
                this.z = Integer.parseInt(split[2]);
                this.file = regionFile;
                this.chunksInFile = this.readFile(protectedChunksWorld);
            } else {
                throw new IllegalStateException("The file " + regionFile.getName() + "isn't a region file");
            }
        } else {
            throw new IllegalStateException("The file " + regionFile.getName() + "isn't a region file");
        }
    }

    /**
     * Check if in this region there are claimed chunks
     *
     * @return true if there are claimed chunks
     */
    public boolean hasClaimedChunks() {
        return this.chunksInFile.stream().anyMatch(ChunkInFile::isProtectedChunk);
    }

    /**
     * Check if in this region there are claimed chunks
     *
     * @return true if there are claimed chunks
     */
    public List<ChunkInFile> getUnclaimedChunks() {
        return this.chunksInFile.stream()
                .filter(chunk -> !chunk.isProtectedChunk()).collect(Collectors.toList());
    }

    /**
     * Deletes the region file
     *
     * @return true if the region file was deleted
     */
    public boolean deleteFile() {
        return this.file.delete();
    }

    public List<ChunkInFile> getChunksInFile() {
        return chunksInFile;
    }

    /**
     * Read the region file
     *
     * @param protectedChunksWorld List of the protected chunks in the world of this region
     * @return List of chunks in the file
     */
    private List<ChunkInFile> readFile(List<Chunk> protectedChunksWorld) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
        List<ChunkInFile> chunks = new LinkedList<>();

        if (raf.length() < SECTOR_SIZE * 2) {
            raf.close();
            return chunks;
        }

        for (int i = 0; i < this.locations.length; i++) {
            this.locations[i] = raf.readInt();
        }

        List<Chunk> protectedChunkRegion = this.filterClaimedChunks(protectedChunksWorld);

        // 32*X <= xChunk < 32*X + 32 -> Inequality of floor(x/32)
        for (int i = 32 * x; i < 32 * x + 32; i++) {
            for (int j = 32 * z; j < 32 * z + 32; j++) {
                boolean isProtectedChunk = this.isProtectedChunk(i, j, protectedChunkRegion);
                ChunkInFile chunkRegion = new ChunkInFile(i, j, isProtectedChunk);
                int location = locations[chunkRegion.getArrayPosition()];

                // location == 0 means that the chunk isn't charged
                if (location != 0) {
                    chunks.add(chunkRegion);
                }
            }
        }
        long claimed = chunks.stream().filter(ChunkInFile::isProtectedChunk).count();
        APlugin.getInstance().info("Region {} has total Chunks {} and {} claimed",
                this, chunks.size(), claimed);
        raf.close();
        return chunks;
    }

    /**
     * Find in the chunk list chunks in this region
     */
    private List<Chunk> filterClaimedChunks(List<Chunk> protectedChunksInWorld) {
        List<Chunk> protectedChunks = new LinkedList<>();
        for (Chunk chunk : protectedChunksInWorld) {
            int xCalculated = Math.floorDiv(chunk.getX(), 32);
            int zCalculated = Math.floorDiv(chunk.getZ(), 32);
            if (this.x == xCalculated && this.z == zCalculated) {
                protectedChunks.add(chunk);
            }
        }
        return protectedChunks;
    }

    /**
     * Checks if the indicated cords are from claimed chunk
     *
     * @param x               the x position
     * @param z               the z position
     * @param protectedChunks List of protected chunks
     * @return true if the chunk is protected
     */
    private boolean isProtectedChunk(int x, int z, List<Chunk> protectedChunks) {
        for (Chunk chunk : protectedChunks) {
            if (chunk.getX() == x && chunk.getZ() == z) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("x: %d, z: %d, file: %s", this.x, this.z, this.file.getName());
    }
}
