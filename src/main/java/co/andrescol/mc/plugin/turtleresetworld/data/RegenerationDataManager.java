package co.andrescol.mc.plugin.turtleresetworld.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * This class allow to save data in a YAML file
 */
public class RegenerationDataManager {

    private static final String FILE_NAME = "regeneration_data.yml";
    private final HashMap<String, WorldRegenerationData> yamlData;

    /**
     * Create the instance
     */
    private RegenerationDataManager() {
        yamlData = loadDataFromFile();
    }

    /**
     * Add chunks to the world section
     *
     * @param world  World
     * @param chunks chunks
     */
    public void addChunks(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.addChunks(chunks);
        this.saveData();
    }

    /**
     * Remove chunks to the world section
     *
     * @param world  World
     * @param chunks chunks
     */
    public void removeChunks(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.removeChunks(chunks);
        mapData.setLastRegeneration(new Date().getTime());
        this.saveData();
    }

    public WorldRegenerationData getDataOf(World world) {
        return this.getDataOfSection(world);
    }

    /**
     * Gets the data of the world's section
     *
     * @param world world
     * @return data
     */
    private WorldRegenerationData getDataOfSection(World world) {
        String name = world.getName();
        if(this.yamlData.containsKey(name)) {
            return this.yamlData.get(name);
        } else {
            WorldRegenerationData data = new WorldRegenerationData();
            this.yamlData.put(name, data);
            this.saveData();
            return data;
        }
    }

    /**
     * Saves the data
     */
    private void saveData() {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            Set<String> keys = this.yamlData.keySet();
            for(String key : keys) {
                yaml.set(key, this.yamlData.get(key).toHashMap());
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.error("The data could not be saved", e);
        }
    }

    /**
     * Load the data from disk
     *
     * @return Regeneration data loaded from disk
     */
    private static HashMap<String, WorldRegenerationData> loadDataFromFile() {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        HashMap<String, WorldRegenerationData> data = new HashMap<>();
        if (file.exists()) {
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(file);
                Set<String> keys = yaml.getKeys(false);
                APlugin.getInstance().info("Key List {}", keys);
                for (String key : keys) {
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    WorldRegenerationData worldData = section == null
                            ? new WorldRegenerationData() : new WorldRegenerationData(section);
                    data.put(key, worldData);
                }
                return data;
            } catch (IOException | InvalidConfigurationException e) {
                plugin.warn("The data of the file {} wasn't read. It will be override", e, FILE_NAME);
            }
        }
        return data;
    }

    // ===================== STATICS =================================
    private static RegenerationDataManager instance;

    public static RegenerationDataManager getInstance() {
        if (instance == null) {
            instance = new RegenerationDataManager();
        }
        return instance;
    }
}
