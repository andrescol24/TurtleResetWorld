package co.andrescol.mc.plugin.turtleresetworld.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.LinkedList;
import java.util.List;

/**
 * This class provide connection with GriefPrevention plugin allowing to get the claimed chunks
 */
public class GriefPreventionClaimer implements Claimer {

    private final List<Chunk> chunks;

    /**
     * Create a instance.
     */
    public GriefPreventionClaimer() {
        GriefPrevention plugin = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
        this.chunks = new LinkedList<>();

        for (Claim claim : plugin.dataStore.getClaims()) {
            chunks.addAll(claim.getChunks());
        }
    }

    @Override
    public List<Chunk> getClaimedChunks() {
        return this.chunks;
    }
}
