package co.andrescol.mc.plugin.turtleresetworld.command.subcommand;

import co.andrescol.mc.library.command.ASubCommand;
import co.andrescol.mc.library.plugin.APlugin;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RegenChunkSubCommand extends ASubCommand {

    public RegenChunkSubCommand() {
        super("chunk", "turtleresetworld.chunk");
    }

    @Override
    public boolean goodUsage(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;
        Chunk chunk = player.getLocation().getChunk();

        ChunkGenerator chunkGenerator = APlugin.getInstance().getDefaultWorldGenerator("world", null);
        ChunkGenerator.ChunkData chunkData = chunkGenerator.generateChunkData(player.getWorld(), new Random(), chunk.getX(), chunk.getZ(), new ChunkGenerator.BiomeGrid() {
            @Override
            public Biome getBiome(int i, int i1) {
                return chunk.getWorld().getBiome(i, i1);
            }

            @Override
            public Biome getBiome(int i, int i1, int i2) {
                return chunk.getWorld().getBiome(i, i1, i2);
            }

            @Override
            public void setBiome(int i, int i1, Biome biome) {
                chunk.getWorld().setBiome(i, i1, biome);
            }

            @Override
            public void setBiome(int i, int i1, int i2, Biome biome) {
                chunk.getWorld().setBiome(i, i1, i2, biome);
            }
        });

        for(int y = 0; y < 255; y++) {
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    BlockData blockData = chunkData.getBlockData(x, y, z);
                    APlugin.getInstance().info("new block data {}", blockData);
                    chunk.getBlock(x, y, z).setBlockData(blockData);
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return new LinkedList<>();
    }
}
