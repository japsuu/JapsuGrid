package org.japsu.japsugrid.chunkgenerators.grid;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class BeforeStructuresGridChunkGenerator extends ChunkGenerator {

    private final int blockSpacing;
    private final boolean removeAllBedrock;
    private final HashSet<Material> nonReplaceableMaterials;

    public BeforeStructuresGridChunkGenerator(int blockSpacing, boolean removeAllBedrock, List<Material> nonReplaceableMaterials){
        this.blockSpacing = blockSpacing;
        this.removeAllBedrock = removeAllBedrock;
        this.nonReplaceableMaterials = new HashSet<>();
        this.nonReplaceableMaterials.addAll(nonReplaceableMaterials);
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

        int minHeight = chunkData.getMinHeight();
        int maxHeight = chunkData.getMaxHeight();
        // Loop the chunk.
        for (int y = minHeight; y < maxHeight; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    int worldX = chunkX * 16 + x;
                    int worldZ = chunkZ * 16 + z;
                    Material currentMaterial = chunkData.getType(x, y, z);

                    if(currentMaterial == Material.AIR)
                        continue;

                    // Delete required blocks.
                    if (worldX % blockSpacing != 0 || y % blockSpacing != 0 || worldZ % blockSpacing != 0) {

                        // Skip blocks found in config.
                        if (nonReplaceableMaterials.contains(currentMaterial)) {
                            continue;
                        }

                        chunkData.setBlock(x, y, z, Material.AIR);

                    } else if (removeAllBedrock && currentMaterial == Material.BEDROCK) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                    }
                }
            }
        }

        // Loop the chunk.
        /*for (int y = chunkData.getMinHeight(); y < chunkData.getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material currentBlock = chunkData.getType(x, y, z);
                    int worldX = chunkX * 16 + x;
                    int worldZ = chunkZ * 16 + z;
                    // Delete required blocks.
                    if(worldX % blockSpacing != 0 || y % blockSpacing != 0 || worldZ % blockSpacing != 0){
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
        }*/
    }
}
