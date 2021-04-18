package co.andrescol.mc.plugin.turtleresetworld.runnable.regen;

import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.plugin.turtleresetworld.runnable.SynchronizeRunnable;
import co.andrescol.mc.plugin.turtleresetworld.runnable.orchestrator.OrchestratorRunnable;
import co.andrescol.mc.plugin.turtleresetworld.util.ChunkInFile;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LoadSchematicRunnable extends SynchronizeRunnable {

    private final ConcurrentLinkedDeque<ChunkInFile> chunks;
    private final World world;

    public LoadSchematicRunnable(OrchestratorRunnable orchestrator,
                                      ConcurrentLinkedDeque<ChunkInFile> chunks, World world) {
        super(orchestrator);
        this.chunks = chunks;
        this.world = world;
    }

    @Override
    protected void execute() throws Exception {
        APlugin plugin = APlugin.getInstance();
        for (ChunkInFile chunk : this.chunks) {
            plugin.info("Loading chunk {} in {}", chunk, world.getName());
            this.loadSchematic(chunk);
        }
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
    private void loadSchematic(ChunkInFile chunk) throws WorldEditException, IOException {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(this.world);

        // Load the schematic
        String fileName = "schematics" + File.separator + world.getName() + File.separator + chunk.getX() +
                "_" + chunk.getZ() + ".schem";
        File file = new File(APlugin.getInstance().getDataFolder(), fileName);
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        }

        // Copy the schematic
        BlockVector3[] positions = this.getLocations(chunk);
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(positions[0])
                    .copyBiomes(true)
                    .copyEntities(true)
                    .build();
            Operations.complete(operation);
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

    public ConcurrentLinkedDeque<ChunkInFile> getChunks() {
        return chunks;
    }

    public World getWorld() {
        return world;
    }
}
