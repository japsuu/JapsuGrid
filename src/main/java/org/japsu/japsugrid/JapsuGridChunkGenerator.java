package org.japsu.japsugrid;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class JapsuGridChunkGenerator extends ChunkGenerator {

    private final int blockSpacing;
    private final boolean removeAllBedrock;
    private final List<Material> nonReplaceableBlocks;

    public JapsuGridChunkGenerator(int blockSpacing, boolean removeAllBedrock, List<Material> nonReplaceableBlocks){
        this.blockSpacing = blockSpacing;
        this.removeAllBedrock = removeAllBedrock;
        this.nonReplaceableBlocks = nonReplaceableBlocks;
    }

    @Override
    public boolean shouldGenerateNoise() { return true; }

    @Override
    public boolean shouldGenerateSurface() { return true; }

    @Override
    public boolean shouldGenerateCaves() { return true; }

    @Override
    public boolean shouldGenerateDecorations() { return true; }

    @Override
    public boolean shouldGenerateMobs() { return true; }

    @Override
    public boolean shouldGenerateStructures() { return true; }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        // Loop the chunk.
        for (int y = chunkData.getMinHeight(); y < chunkData.getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material currentBlock = chunkData.getType(x, y, z);
                    // Delete required blocks.
                    if(x % blockSpacing != 0 || y % blockSpacing != 0 || z % blockSpacing != 0){
                        // Skip blocks found in config.
                        if(nonReplaceableBlocks.contains(currentBlock)){
                            continue;
                        }
                        chunkData.setBlock(x, y, z, Material.AIR);
                    } else if (removeAllBedrock && currentBlock == Material.BEDROCK) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }
}
