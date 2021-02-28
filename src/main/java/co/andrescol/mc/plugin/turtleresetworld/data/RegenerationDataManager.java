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

    private final HashMap<String, WorldRegenerationData> yamlData;
    private final String fileName;

    /**
     * Create the instance
     * @param fileName File name for the regeneration data.
     */
    private RegenerationDataManager(FileName fileName) {
        this.fileName = fileName.getFileName();
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
        File file = new File(plugin.getDataFolder(), this.fileName);
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
    private HashMap<String, WorldRegenerationData> loadDataFromFile() {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), this.fileName);
        HashMap<String, WorldRegenerationData> data = new HashMap<>();
        if (file.exists()) {
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(file);
                Set<String> keys = yaml.getKeys(false);
                for (String key : keys) {
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    WorldRegenerationData worldData = section == null
                            ? new WorldRegenerationData() : new WorldRegenerationData(section);
                    data.put(key, worldData);
                }
                return data;
            } catch (IOException | InvalidConfigurationException e) {
                plugin.warn("The data of the file {} wasn't read. It will be override", e, this.fileName);
            }
        }
        return data;
    }

    // ===================== STATICS =================================
    private static RegenerationDataManager instanceForRegen;
    private static RegenerationDataManager instanceForCopy;

    /**
     * Get the instance
     * @param forFile Enum that define the instance needed
     * @return instance of this class
     */
    public static RegenerationDataManager getInstance(FileName forFile) {
        if (forFile == FileName.FILE_NAME_COPY) {
            if (instanceForCopy == null) {
                instanceForCopy = new RegenerationDataManager(forFile);
            }
            return instanceForCopy;
        }

        if (instanceForRegen == null) {
            instanceForRegen = new RegenerationDataManager(forFile);
        }
        return instanceForRegen;
    }
}
