package co.andrescol.mc.plugin.turtleresetworld.hooks;

import org.bukkit.Chunk;

import java.util.List;

/**
 * This interfaces allow to get the claimed chunks by other plugin
 */
public interface Claimer {

    /**
     *
     * @return List of claimed chunks
     */
    List<Chunk> getClaimedChunks();
}
