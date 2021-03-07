package co.andrescol.mc.plugin.turtleresetworld.util;

import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;

public class TimeCalculator {

    private int totalChunks;

    /**
     * Calculates the time of regeneration
     * @param worlds List of world
     * @return The time in seconds
     */
    public long calculate(List<World> worlds) {
//        List<ChunkInFile> chunksToRegen = new LinkedList<>();
//        for(World world : worlds) {
//            WorldFilesProcess process = new WorldFilesProcess(world);
//            chunksToRegen.addAll(process.getChunksToRegen());
//        }
//
//        this.totalChunks = chunksToRegen.size();
//        int executableSize = APlugin.getInstance().getConfig().getInt("chunksPerThread");
//        int numberExecutables = totalChunks / executableSize + 1;
//        int executeTime = numberExecutables * executableSize; // 1 chunks per second
//
//        int graceTime = APlugin.getInstance().getConfig().getInt("timeOfGraceForServer.chunkRegen");
//        graceTime = (graceTime / 20) * numberExecutables; // 20 tics per second
//
//        // World creation + grace time + executables time + other time
//        double total = 30 * worlds.size() + graceTime + executeTime + 60;
//        return (long) Math.ceil(total);
        return 0;
    }

    public int getTotalChunks() {
        return totalChunks;
    }
}
