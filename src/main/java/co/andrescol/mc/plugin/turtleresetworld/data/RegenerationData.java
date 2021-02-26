package co.andrescol.mc.plugin.turtleresetworld.data;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class with the information of storage
 *
 * @author xX_andrescol_Xx
 */
public class RegenerationData {

    private long lastRegeneration;
    private boolean lastRegenerationSuccessFully;
    private final HashMap<String, List<String>> chunksToRegen;

    public RegenerationData() {
        this.chunksToRegen = new HashMap<>();
    }

    /**
     * Create an instance using an yaml configuration
     *
     * @param yaml the yaml info
     */
    public RegenerationData(YamlConfiguration yaml) {
        this.lastRegeneration = yaml.getInt("lastRegeneration");
        this.lastRegenerationSuccessFully = yaml.getBoolean("lastRegenerationSuccessFully");
        this.chunksToRegen = new HashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("chunksToRegen");
        if (section != null) {
            Set<String> worldsName = section.getKeys(false);
            for (String world : worldsName) {
                List<String> chunks = section.getStringList(world);
                this.chunksToRegen.put(world, chunks);
            }
        } else {
            APlugin.getInstance().warn("The section of chunksToRegen does not exist");
        }

    }

    /**
     * Adds chunks to regen in the world
     *
     * @param chunks chunks
     * @param world  world
     */
    public void addChunksToRegen(List<ChunkInFile> chunks, World world) {
        List<String> converted = this.convertToString(chunks);
        if (this.chunksToRegen.containsKey(world.getName())) {
            List<String> actual = this.chunksToRegen.get(world.getName());
            actual.addAll(converted);
            this.chunksToRegen.put(world.getName(), actual);
        } else {
            this.chunksToRegen.put(world.getName(), converted);
        }
    }

    /**
     * Gets the list of chunks to regen in the world
     *
     * @param world The world
     * @return The list of chunks to regen in the world
     */
    public List<ChunkInFile> getChunksToRegen(World world) {
        if (this.chunksToRegen.containsKey(world.getName())) {
            return this.convertToChunk(this.chunksToRegen.get(world.getName()));
        }
        return new LinkedList<>();
    }

    /**
     * Adds chunks to regen in the world
     *
     * @param chunks chunks
     * @param world  world
     */
    public void removeChunksToRegen(ConcurrentLinkedDeque<ChunkInFile> chunks, World world) {
        if (this.chunksToRegen.containsKey(world.getName())) {
            List<String> converted = this.convertToString(chunks);
            List<String> actual = this.chunksToRegen.get(world.getName());
            actual.removeAll(converted);
            this.chunksToRegen.put(world.getName(), actual);
        }
    }

    public void setLastRegeneration(long lastRegeneration) {
        this.lastRegeneration = lastRegeneration;
    }

    public void setLastRegenerationSuccessFully(boolean lastRegenerationSuccessFully) {
        this.lastRegenerationSuccessFully = lastRegenerationSuccessFully;
    }

    /**
     * Convert the list of int array to list of chunk
     *
     * @param chunks chunks
     * @return list of chunks in array
     */
    private List<String> convertToString(Collection<ChunkInFile> chunks) {
        List<String> converted = new LinkedList<>();
        chunks.forEach(chunk ->
                converted.add(String.format("%d %d %b", chunk.getX(), chunk.getZ(), chunk.isProtectedChunk())));
        return converted;
    }

    /**
     * Convert the list of chunk to list of chunk in arrays
     *
     * @param chunks chunks
     * @return list of chunks in array
     */
    private List<ChunkInFile> convertToChunk(List<String> chunks) {
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

    public YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("lastRegeneration", this.lastRegeneration);
        yaml.set("lastRegenerationSuccessFully", this.lastRegenerationSuccessFully);
        yaml.set("chunksToRegen", this.chunksToRegen);
        return yaml;
    }
}
