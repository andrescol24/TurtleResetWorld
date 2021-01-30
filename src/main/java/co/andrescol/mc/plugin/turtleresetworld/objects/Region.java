package co.andrescol.mc.plugin.turtleresetworld.objects;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.hooks.Claimer;
import org.bukkit.Chunk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class facility the region process.
 * Read https://minecraft.gamepedia.com/Region_file_format for more information
 */
public class Region {

    /**
     * offsets in 4KiB sector. ex:
     * location = 2 -> chunk starts in 8192
     */
    private static final int SECTOR_SIZE = 4096;

    private final int x;
    private final int z;
    private File file;
    private ConcurrentLinkedDeque<ChunkRegion> chunks;
    private boolean fileLoaded;

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

    private byte[] data;

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
        for (Claimer claimer : claimers) {
            for (Chunk chunk : claimer.getClaimedChunks()) {
                int xCalculated = Math.floorDiv(chunk.getX(), 32);
                int zCalculated = Math.floorDiv(chunk.getZ(), 32);
                if (this.x == xCalculated && this.z == zCalculated) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Delete the unclaimed chunks. If the chunks don't have been
     * loaded from the file it will load first
     *
     * @param claimers claimers
     * @return List of removed chunks in the region
     * @throws IOException Throws this exception is occurs any error reading the file.
     */
    public List<ChunkRegion> removeUnClaimedChunks(List<Claimer> claimers) throws IOException {
        if (this.chunks == null) {
            this.readFile();
        }

        List<ChunkRegion> removed = new LinkedList<>();
        for (Claimer claimer : claimers) {
            List<Chunk> claimedChunks = claimer.getClaimedChunks();
            for (ChunkRegion chunkRegion : this.chunks) {
                boolean isClaimed = claimedChunks.stream().anyMatch(
                        x -> x.getX() == chunkRegion.getX() && x.getZ() == chunkRegion.getZ());
                if (!isClaimed) {
                    removed.add(chunkRegion);
                    this.locations[chunkRegion.getRegionLocation()] = 0;
                    this.chunks.remove(chunkRegion);
                }
            }
        }
        APlugin.getInstance().info("new location array {}", Arrays.toString(this.locations));
        return removed;
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

        this.chunks = new ConcurrentLinkedDeque<>();
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                ChunkRegion chunkRegion = new ChunkRegion(x, z);
                int location = locations[chunkRegion.getRegionLocation()];
                // location == 0 means that the chunk isn't charged
                if (location != 0) {
                    this.chunks.add(chunkRegion);
                }
            }
        }
        // The file length in always less than 1MiB. The data is after the locations and timestamps
        int len = (int) raf.length() - SECTOR_SIZE * 2;
        this.data = new byte[len];

        raf.readFully(this.data);
        raf.close();
        this.fileLoaded = true;
    }

    /**
     * Saves the file
     *
     * @throws IOException This will be throw if occurs any O/I error
     */
    public void saveFile() throws IOException {
        if (!this.fileLoaded) {
            return;
        }

        RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
        for (int location : locations) {
            raf.writeInt(location);
        }
        for (int timestamp : timestamps) {
            raf.writeInt(timestamp);
        }
        raf.write(this.data);
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

//    /**
//     * Reads the chunk data
//     *
//     * @param file File with the pointer set
//     * @param len  len of the chunk data
//     * @return the chunk data
//     * @throws IOException
//     */
//    private byte[] readChunkData(RandomAccessFile file, int len) throws IOException {
//        byte[] data = new byte[len];
//        file.readFully(data, 0, len);
//        return data;
//    }

    @Override
    public String toString() {
        return String.format("x: %d, z: %d, file: %s", this.x, this.z, this.file.getName());
    }
}
