package co.andrescol.mc.plugin.turtleresetworld.data;

import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class WorldRegenerationData {

    private long lastRegeneration;
    private final List<String> chunksToSaveSchematic;
    private final List<String> chunksToLoadSchematic;

    WorldRegenerationData() {
        this.chunksToSaveSchematic = new LinkedList<>();
        this.chunksToLoadSchematic = new LinkedList<>();
    }

    WorldRegenerationData(ConfigurationSection section) {
        this.lastRegeneration = section.getLong("lastRegeneration");
        this.chunksToSaveSchematic = section.getStringList("chunksToSaveSchematic");
        this.chunksToLoadSchematic = section.getStringList("chunksToLoadSchematic");
    }

    public long getLastRegeneration() {
        return lastRegeneration;
    }

    public List<ChunkInFile> getChunksToSaveSchematic() {
        return convertToChunk(this.chunksToSaveSchematic);
    }

    void setLastRegeneration(long lastRegeneration) {
        this.lastRegeneration = lastRegeneration;
    }

    void addChunks(Collection<ChunkInFile> chunks) {
        this.chunksToSaveSchematic.addAll(convertToString(chunks));
    }

    void removeChunks(Collection<ChunkInFile> chunks) {
        this.chunksToSaveSchematic.removeAll(convertToString(chunks));
    }

    /**
     * Convert the list of chunk to list of chunk in arrays
     *
     * @param chunks chunks
     * @return list of chunks in array
     */
    private static List<ChunkInFile> convertToChunk(Collection<String> chunks) {
        List<ChunkInFile> converted = new LinkedList<>();
        chunks.forEach(chunk -> {
            String[] data = chunk.split(" ");
            int x = Integer.parseInt(data[0]);
            int z = Integer.parseInt(data[1]);
            converted.add(new ChunkInFile(x, z, false));
        });
        return converted;
    }

    /**
     * Convert the list of int array to list of chunk
     *
     * @param chunks chunks
     * @return list of chunks in array
     */
    private static List<String> convertToString(Collection<ChunkInFile> chunks) {
        List<String> converted = new LinkedList<>();
        chunks.forEach(chunk ->
                converted.add(String.format("%d %d", chunk.getX(), chunk.getZ())));
        return converted;
    }

    /**
     * Convert this object to a Map
     * @return Map
     */
    public Map<?,?> toHashMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("lastRegeneration", this.lastRegeneration);
        result.put("chunksToRegen", this.chunksToSaveSchematic);
        return result;
    }
}
