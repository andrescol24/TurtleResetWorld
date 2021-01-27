package co.andrescol.mc.plugin.turtleresetworld.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.LinkedList;
import java.util.List;

public class GriefPreventionClaimer implements Claimer {

    @Override
    public List<Chunk> getClaimedChunks() {
        GriefPrevention plugin = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
        List<Chunk> chunks = new LinkedList<>();
        for(Claim claim : plugin.dataStore.getClaims()) {
            chunks.addAll(claim.getChunks());
        }
        return chunks;
    }
}
