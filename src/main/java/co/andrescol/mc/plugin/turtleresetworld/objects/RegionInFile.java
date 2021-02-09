package co.andrescol.mc.plugin.turtleresetworld.objects;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Chunk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class facility the region file process.
 * Read https://minecraft.gamepedia.com/Region_file_format for more information
 */
public class RegionInFile {

    private final int x;
    private final int z;
    private final File file;
    private final List<Chunk> protectedChunks;
    private ConcurrentLinkedDeque<ChunkInFile> chunksInFile;

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
     * timestamps is 4 bytes per chunk. 4096 - 8191 bytes
     */
    private final int[] timestamps = new int[1024];

    /**
     * Create an instance of this class
     *
     * @param regionFile Region file
     */
    public RegionInFile(File regionFile, List<Chunk> protectedChunksWorld) {
        if (regionFile.getName().endsWith(".mca")) {
            String[] split = regionFile.getName().split("\\.");
            if (split.length == 4) {
                this.x = Integer.parseInt(split[1]);
                this.z = Integer.parseInt(split[2]);
                this.file = regionFile;
                this.protectedChunks = this.filterClaimedChunks(protectedChunksWorld);
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
        return !this.protectedChunks.isEmpty();
    }

    /**
     * Delete the unclaimed chunks in the region file
     *
     * @throws IOException Throws this exception is occurs any error reading the file.
     */
    public void removeUnClaimedChunksInFile() throws IOException {
        this.readFile();
        this.writeFile();
    }

    /**
     * Read the region file
     */
    private void readFile() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
        if (raf.length() < SECTOR_SIZE * 2) {
            raf.close();
            return;
        }
        for (int i = 0; i < this.locations.length; i++) {
            this.locations[i] = raf.readInt();
        }
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = raf.readInt();
        }

        this.chunksInFile = new ConcurrentLinkedDeque<>();

        // 32*X <= xChunk < 32*X + 32 -> Inequality of floor(x/32)
        for (int i = 32 * x; i < 32 * x + 32; i++) {
            for (int j = 32 * z; j < 32 * z + 32; j++) {
                boolean isProtectedChunk = this.isProtectedChunk(i, j);
                ChunkInFile chunkRegion = new ChunkInFile(i, j, isProtectedChunk);
                int location = locations[chunkRegion.getArrayPosition()];

                // location == 0 means that the chunk isn't charged
                if (location != 0) {
                    long positionFile = (long) (location >> 8) * SECTOR_SIZE;
                    raf.seek(positionFile);

                    // Reading the chunk data
                    int chunkSize = raf.readInt();
                    chunkRegion.setChunkSize(chunkSize);

                    byte compressionType = raf.readByte();
                    chunkRegion.setCompressionType(compressionType);

                    byte[] data = new byte[chunkSize-1];
                    raf.readFully(data, 0, chunkSize-1);
                    chunkRegion.setData(data);
                    this.chunksInFile.add(chunkRegion);
                }
            }
        }
        long claimed = this.chunksInFile.stream().filter(ChunkInFile::isProtectedChunk).count();
        APlugin.getInstance().info("Total Chunks {}, {} claimed", this.chunksInFile.size(), claimed);
        raf.close();
    }

    private void writeFile() throws IOException {
        APlugin.getInstance().info("Writing file without unclaimed chunks");
        RandomAccessFile raf = new RandomAccessFile(this.file, "rw");

        int actualPosition = 2;
        List<ChunkInFile> listInOrder = new ArrayList<>();

        for(ChunkInFile chunk : this.chunksInFile) {
            if(!chunk.isProtectedChunk()) {
                APlugin.getInstance().info("Ignoring chunk {}", chunk);
                locations[chunk.getArrayPosition()] = 0;
                timestamps[chunk.getArrayPosition()] = 0;
            } else {
                APlugin.getInstance().info("Writing chunk {} in the position {}", chunk, actualPosition);
                locations[chunk.getArrayPosition()] = (actualPosition << 8) | chunk.getChunkSize();

                // calculation the next position of the next chunk it has to be multiple of SECTOR_SIZE
                int newPosition = this.calculateNumberOfSector(chunk);
                listInOrder.add(chunk);
                actualPosition += newPosition;
            }
        }

        for (int location : this.locations) {
            raf.write(location);
        }
        for (int timestamp : this.timestamps) {
            raf.write(timestamp);
        }

        for(ChunkInFile chunk : listInOrder) {
            raf.write(chunk.getChunkSize());
            raf.write(chunk.getCompressionType());
            raf.write(chunk.getData());
            byte[] padding = new byte[this.calculateChunkPadding(chunk)];
            raf.write(padding);
        }
        raf.close();
    }

    /**
     * Deletes the region file
     *
     * @return true if the region file was deleted
     */
    public boolean deleteFile() {
        return this.file.delete();
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


    private int calculateChunkPadding(ChunkInFile chunk) {
        return calculateNumberOfSector(chunk) * SECTOR_SIZE - 5 - chunk.getChunkSize();
    }

    private int calculateNumberOfSector(ChunkInFile chunk) {
        // Location (4Bytes) + Compression type (1Byte) + Data
        int chunkDataWithSize = chunk.getChunkSize() + 5;
        int numberBlocks = chunkDataWithSize / SECTOR_SIZE;

        if (numberBlocks % SECTOR_SIZE == 0) {
            return numberBlocks;
        } else {
            APlugin.getInstance().warn(
                    "no multiple-of-4096B {} for {}, mod: {}",
                    numberBlocks, chunk, numberBlocks % SECTOR_SIZE);
            return numberBlocks + 1;
        }
    }

    /**
     * Checks if the indicated cords are from claimed chunk
     *
     * @param x the x position
     * @param z the z position
     * @return true if the chunk is protected
     */
    private boolean isProtectedChunk(int x, int z) {
        for (Chunk chunk : this.protectedChunks) {
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
