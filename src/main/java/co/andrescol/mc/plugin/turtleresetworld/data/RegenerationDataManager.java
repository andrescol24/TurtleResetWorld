package co.andrescol.mc.plugin.turtleresetworld.data;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class allow to save data in a YAML file
 */
public class RegenerationDataManager {

    private static final String CONTINUE_LOADING_KEY = "continueLoading";
    private static final String TELEPORTED_PLAYERS = "teleportedPlayers";

    private HashMap<String, WorldRegenerationData> chunksData;
    private List<String> teleportedPlayers;
    private boolean continueLoading;
    private final String fileName;

    /**
     * Create the instance
     */
    private RegenerationDataManager() {
        this.fileName = "regeneration_data.yml";
        this.readData();
    }

    /**
     * Add chunks to the world section for save in the schematics
     *
     * @param world  World
     * @param chunks chunks
     */
    public void addChunksSaveSchematics(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.addChunksToSaveSchematic(chunks);
        this.saveData();
    }

    /**
     * Remove chunks to the world section
     *
     * @param world  World
     * @param chunks chunks
     */
    public void removeChunksSaveSchematics(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.removeChunksToSaveSchematic(chunks);
        mapData.setLastRegeneration(new Date().getTime());
        this.saveData();
    }

    /**
     * Add chunks to the world section for save in the schematics
     *
     * @param world  World
     * @param chunks chunks
     */
    public void addChunksLoadSchematics(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.addChunksToLoadSchematics(chunks);
        this.saveData();
    }

    /**
     * Remove chunks to the world section
     *
     * @param world  World
     * @param chunks chunks
     */
    public void removeChunksLoadSchematics(World world, Collection<ChunkInFile> chunks) {
        WorldRegenerationData mapData = this.getDataOfSection(world);
        mapData.removeChunksToLoadSchematics(chunks);
        mapData.setLastRegeneration(new Date().getTime());
        this.saveData();
    }

    /**
     * Set the flag to continue loading chunks or not
     *
     * @param continueLoading boolean
     */
    public void setContinueLoading(boolean continueLoading) {
        this.continueLoading = continueLoading;
        this.saveData();
    }

    /**
     * Get the list of worlds that they have chunks pending to lead
     *
     * @return List of worlds with chunks pending to lead
     */
    public List<World> getListWorldsPending() {
        List<World> worlds = new LinkedList<>();
        for (String key : this.chunksData.keySet()) {
            WorldRegenerationData data = this.chunksData.get(key);
            if (!data.getChunksToLoadSchematic().isEmpty()) {
                World world = Bukkit.getWorld(key);
                worlds.add(world);
            }
        }
        return worlds;
    }

    /**
     * Checks if the player with that name has been teleport
     *
     * @param name the nick of the player
     * @return true if the player already has been teleport
     */
    public boolean isTeleportedPlayer(String name) {
        return this.teleportedPlayers.contains(name);
    }

    /**
     * Add the nick of the player to the list of teleported players
     *
     * @param name nick of the player
     */
    public void addTeleportedPlayer(String name) {
        this.teleportedPlayers.add(name);
        this.saveData();
    }

    /**
     * Clear the list of teleported players
     */
    public void clearTeleportedPlayerList() {
        this.teleportedPlayers.clear();
        this.saveData();
    }

    private void readData() {
        APlugin plugin = APlugin.getInstance();
        HashMap<String, WorldRegenerationData> data = new HashMap<>();
        boolean continueLoading = false;
        List<String> teleportedPlayers = new LinkedList<>();
        File file = new File(plugin.getDataFolder(), this.fileName);
        if (file.exists()) {
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(file);
                Set<String> keys = yaml.getKeys(false);
                for (String key : keys) {
                    if (key.equals(CONTINUE_LOADING_KEY)) {
                        continueLoading = yaml.getBoolean(CONTINUE_LOADING_KEY);
                    } else if (key.equals(TELEPORTED_PLAYERS)) {
                        teleportedPlayers = yaml.getStringList(TELEPORTED_PLAYERS);
                    } else {
                        ConfigurationSection section = yaml.getConfigurationSection(key);
                        WorldRegenerationData worldData = section == null
                                ? new WorldRegenerationData() : new WorldRegenerationData(section);
                        data.put(key, worldData);
                    }
                }
            } catch (IOException | InvalidConfigurationException e) {
                plugin.warn("The data of the file {} wasn't read. It will be override", e, this.fileName);
            }
        }
        this.chunksData = data;
        this.continueLoading = continueLoading;
        this.teleportedPlayers = teleportedPlayers;
    }

    public boolean isContinueLoading() {
        return this.continueLoading;
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
        if(this.chunksData.containsKey(name)) {
            return this.chunksData.get(name);
        } else {
            WorldRegenerationData data = new WorldRegenerationData();
            this.chunksData.put(name, data);
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
            Set<String> keys = this.chunksData.keySet();
            for(String key : keys) {
                yaml.set(key, this.chunksData.get(key).toHashMap());
            }
            yaml.set(CONTINUE_LOADING_KEY, this.continueLoading);
            yaml.set(TELEPORTED_PLAYERS, this.teleportedPlayers);
            yaml.save(file);
        } catch (IOException e) {
            plugin.error("The data could not be saved", e);
        }
    }

    // ===================== STATICS =================================
    private static RegenerationDataManager instance;

    /**
     * Get the instance
     *
     * @return instance of this class
     */
    public static RegenerationDataManager getInstance() {
        if (instance == null) {
            instance = new RegenerationDataManager();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
