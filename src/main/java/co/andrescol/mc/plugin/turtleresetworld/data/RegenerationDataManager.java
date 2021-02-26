package co.andrescol.mc.plugin.turtleresetworld.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

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
    private final YamlConfiguration yamlData;

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
        this.saveData(world.getName(), mapData);
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
        this.saveData(world.getName(), mapData);
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
        ConfigurationSection section = this.yamlData.getConfigurationSection(name);
        return section != null ? new WorldRegenerationData(section) : new WorldRegenerationData();
    }

    /**
     * Saves the data
     */
    private void saveData(String section, WorldRegenerationData data) {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        try {
            this.yamlData.set(section, data.toHashMap());
            this.yamlData.save(file);
        } catch (IOException e) {
            plugin.error("The data could not be saved", e);
        }
    }

    /**
     * Load the data from disk
     *
     * @return Regeneration data loaded from disk
     */
    private static YamlConfiguration loadDataFromFile() {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        YamlConfiguration yaml = new YamlConfiguration();
        if (file.exists()) {
            try {
                yaml.load(file);
                return yaml;
            } catch (IOException | InvalidConfigurationException e) {
                plugin.warn("The data of the file {} wasn't read. It will be override", e, FILE_NAME);
            }
        }
        return yaml;
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
