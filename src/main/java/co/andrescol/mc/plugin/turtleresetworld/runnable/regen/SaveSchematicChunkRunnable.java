package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SaveSchematicChunkRunnable extends SynchronizeRunnable {

    private final ConcurrentLinkedDeque<ChunkInFile> chunks;
    private final World world;
    private boolean success;

    public SaveSchematicChunkRunnable(OrchestratorRunnable orchestrator,
                                      ConcurrentLinkedDeque<ChunkInFile> chunks, World world) {
        super(orchestrator);
        this.chunks = chunks;
        this.world = world;
    }

    @Override
    protected void execute() throws Exception {
        APlugin plugin = APlugin.getInstance();
        for (ChunkInFile chunk : this.chunks) {
            plugin.info("Saving {} chunk schematic", chunk);
            this.saveSchematic(chunk);
        }
        this.success = true;
        this.orchestrator.setTotalChunks(this.orchestrator.getTotalChunks() - this.chunks.size());
        plugin.info("{} chunks left!", this.orchestrator.getTotalChunks());
    }

    /**
     * Save the schematic of the chunk
     *
     * @param chunk chunk
     * @throws WorldEditException Threw if there was an error completing the operation
     * @throws IOException        Threw if there was an error saving the file
     */
    private void saveSchematic(ChunkInFile chunk) throws WorldEditException, IOException {
        BlockVector3[] positions = this.getLocations(chunk); // Gets the positions of the region to copy
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(this.world);
        CuboidRegion regionFrom = new CuboidRegion(world, positions[0], positions[1]);
        Chunk minecraftChunk = this.world.getChunkAt(chunk.getX(), chunk.getZ());
        minecraftChunk.load();
        BlockArrayClipboard clipboard = new BlockArrayClipboard(regionFrom);
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, regionFrom, clipboard, regionFrom.getMinimumPoint());
            forwardExtentCopy.setCopyingBiomes(true);
            forwardExtentCopy.setCopyingEntities(true);
            Operations.complete(forwardExtentCopy);
        }
        minecraftChunk.unload();

        String fileName = "schematics" + File.separator + world.getName() + File.separator + chunk.getX() +
                "_" + chunk.getZ() + ".schem";
        File file = new File(APlugin.getInstance().getDataFolder(), fileName);
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        APlugin.getInstance().info("Entities saving: {}", clipboard.getEntities().size());

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
    }

    /**
     * Gets the min and max location of the WorldEdit region to copy
     *
     * @param chunk chunk to copy
     * @return An array with the min and max BlockVector3 in the 0 and 1st position correspondent
     */
    private BlockVector3[] getLocations(ChunkInFile chunk) {
        int x = chunk.getX() * 16;
        int z = chunk.getZ() * 16;
        int y = APlugin.getInstance().getConfig().getInt("minYProtected");
        BlockVector3 min = BlockVector3.at(x, y, z);

        x = x + 15;
        z = z + 15;
        y = 255;
        BlockVector3 max = BlockVector3.at(x, y, z);
        return new BlockVector3[]{min, max};
    }

    public boolean isSuccess() {
        return success;
    }

    public ConcurrentLinkedDeque<ChunkInFile> getChunks() {
        return chunks;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public long getDelay() {
        return 160;
    }
}
