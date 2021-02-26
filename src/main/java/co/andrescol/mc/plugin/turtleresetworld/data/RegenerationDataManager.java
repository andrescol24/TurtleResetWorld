package co.andrescol.mc.plugin.turtleresetworld.data;

import java.io.File;
import java.io.IOException;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class RegenerationDataManager {

    private RegenerationData data;

    private RegenerationDataManager() {}

    /**
     * Gets the data reading the file
     *
     * @return The regeneration data
     * @throws ReadingDataException Threw if there was an error reading the file
     */
    public RegenerationData getData() throws ReadingDataException {
        if (data == null) {
            data = this.loadDataFromFile();
        }
        return data;
    }

    /**
     * Saves the data
     */
    public void saveData() {
        if (data != null) {
            APlugin plugin = APlugin.getInstance();
            File file = new File(plugin.getDataFolder(), "data.yml");
            try {
                YamlConfiguration yaml = data.toYaml();
                yaml.save(file);
            } catch (IOException e) {
                plugin.error("The data could not be saved", e);
            }
        }
    }

    /**
     * Load the data from disk
     *
     * @return Regeneration data loaded from disk
     */
    private RegenerationData loadDataFromFile() throws ReadingDataException {
        APlugin plugin = APlugin.getInstance();
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (file.exists()) {
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(file);
                return new RegenerationData(yaml);
            } catch (IOException | InvalidConfigurationException e) {
                throw new ReadingDataException(e);
            }
        } else {
            return new RegenerationData();
        }
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
