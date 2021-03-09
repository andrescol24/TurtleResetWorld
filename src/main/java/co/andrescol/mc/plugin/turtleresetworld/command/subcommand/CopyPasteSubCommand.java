package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.plugin.APlugin;
import co.andrescol.mc.library.utils.AUtils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class CopyPasteSubCommand extends ASubCommand {

    public CopyPasteSubCommand() {
        super("copy", "turtleresetworld.copy");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        String[] chunkFrom = args[1].split("/");
        String[] chunkTo = args[2].split("/");
        World bworld = Bukkit.getWorld("world");

        Chunk chunk = bworld.getChunkAt(Integer.parseInt(chunkFrom[0]), Integer.parseInt(chunkFrom[1]));
        chunk.load();

        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bworld);

        BlockVector3[] fromPositions = this.getLocations(chunkFrom);
        CuboidRegion regionFrom = new CuboidRegion(world, fromPositions[0], fromPositions[1]);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(regionFrom);
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, regionFrom, clipboard, regionFrom.getMinimumPoint());
            forwardExtentCopy.setCopyingBiomes(true);
            forwardExtentCopy.setCopyingEntities(true);
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            APlugin.getInstance().error("Could not copy chunk", e);
            AUtils.sendMessage(commandSender, "No se pudo copiar el chunk");
            return true;
        }
        APlugin.getInstance().info("Entities copying: {}", clipboard.getEntities().size());

        BlockVector3[] toPositions = this.getLocations(chunkTo);
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(toPositions[0])
                    .copyBiomes(true)
                    .copyEntities(true)
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            APlugin.getInstance().error("Could not paste", e);
            AUtils.sendMessage(commandSender, "No se pudo pegar el chunk");
            return true;
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return new LinkedList<>();
    }

    @Override
    public boolean goodUsage(CommandSender commandSender, Command command, String label, String[] args) {
        return args.length == 3;
    }

    private BlockVector3[] getLocations(String[] chunk) {
        int x = Integer.parseInt(chunk[0]) * 16;
        int z = Integer.parseInt(chunk[1]) * 16;
        int y = 0;
        APlugin.getInstance().info("Getting min position at x:{} y:{} z:{}", x, y ,z);
        BlockVector3 min = BlockVector3.at(x, y, z);

        x = x + 15;
        z = z + 15;
        y = 255;
        APlugin.getInstance().info("Getting max position at x:{} y:{} z:{}", x, y ,z);
        BlockVector3 max = BlockVector3.at(x, y, z);
        return new BlockVector3[]{min, max};
    }
}
