package co.andrescol.mc.plugin.turtleresetworld.data;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class WorldRegenerationData {

    private long lastRegeneration;
    private final List<String> chunksToRegen;

    WorldRegenerationData() {
        APlugin.getInstance().info("Empty constructor");
        this.chunksToRegen = new LinkedList<>();
    }

    WorldRegenerationData(ConfigurationSection section) {
        this.lastRegeneration = section.getInt("lastRegeneration");
        this.chunksToRegen = section.getStringList("chunksToRegen");
        APlugin.getInstance().info("Section constructor {}", this.chunksToRegen.size());
    }

    public long getLastRegeneration() {
        return lastRegeneration;
    }

    public List<ChunkInFile> getChunksToRegen() {
        return convertToChunk(this.chunksToRegen);
    }

    void setLastRegeneration(long lastRegeneration) {
        this.lastRegeneration = lastRegeneration;
    }

    void addChunks(Collection<ChunkInFile> chunks) {
        this.chunksToRegen.addAll(convertToString(chunks));
    }

    void removeChunks(Collection<ChunkInFile> chunks) {
        this.chunksToRegen.removeAll(convertToString(chunks));
        APlugin.getInstance().info("New size {}", this.chunksToRegen.size());
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
            boolean protectedChunk = Boolean.parseBoolean(data[2]);
            converted.add(new ChunkInFile(x, z, protectedChunk));
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
        result.put("chunksToRegen", this.chunksToRegen);
        return result;
    }
}
