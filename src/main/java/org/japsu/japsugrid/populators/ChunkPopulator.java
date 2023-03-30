package org.japsu.japsugrid.populators;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;

public class ChunkPopulator extends BlockPopulator {

    private final int blockSpacingInterval;
    private final boolean removeAllBedrock;
    private final HashSet<Material> nonReplaceableMaterials;

    public ChunkPopulator(int blockSpacingInterval, boolean removeAllBedrock, HashSet<Material> nonReplaceableMaterials) {
        this.blockSpacingInterval = blockSpacingInterval;
        this.removeAllBedrock = removeAllBedrock;
        this.nonReplaceableMaterials = nonReplaceableMaterials;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {

        populateSimple(worldInfo, chunkX, chunkZ, limitedRegion);
    }

    private void populateSimple(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {

        int minHeight = worldInfo.getMinHeight();
        int maxHeight = worldInfo.getMaxHeight();
        int buffer = limitedRegion.getBuffer();

        // Note: If we wouldn't use world-space coordinates to calculate where the grid should be, we could
        // reduce the amount of loop iterations drastically.
        // I'm fine with sacrificing a bit of performance for both readability and a world space grid.

        // Precalculate block position base coordinates.
        int worldXBase = chunkX * 16;
        int worldZBase = chunkZ * 16;

        // We generate the grid, accessing the neighbouring chunks too.
        // More precisely, each time this ChunkPopulator is executed for a chunk, it is executed for 3x3 area of
        // chunks with the "caller" chunk being the middle one.
        //
        // This is done because chunk population might be spread across multiple chunks, causing already "gridified"
        // chunks to receive feature placements.
        //
        // As far as I know there's sadly currently no (simple) way around this, without dabbling with NMS.
        for (int x = -buffer; x < 16 + buffer; x++) {
            for (int z = -buffer; z < 16 + buffer; z++) {

                // Calculate horizontal position from base.
                int worldX = worldXBase + x;
                int worldZ = worldZBase + z;

                // Loop vertically.
                for (int y = minHeight; y < maxHeight; y++) {

                    Material material = limitedRegion.getType(worldX, y, worldZ);

                    // Skip all air blocks, no need to change those.
                    if(material == Material.AIR)
                        continue;

                    // Delete blocks that fall outside the grid pattern.
                    if (worldX % blockSpacingInterval != 0 || y % blockSpacingInterval != 0 || worldZ % blockSpacingInterval != 0) {

                        // Skip deletion for blocks found in config.
                        if (nonReplaceableMaterials.contains(material)) {
                            continue;
                        }

                        limitedRegion.setType(worldX, y, worldZ, Material.AIR);

                        // Remove bedrock if required.
                    } else if (removeAllBedrock && material == Material.BEDROCK) {
                        limitedRegion.setType(worldX, y, worldZ, Material.AIR);
                    }
                }
            }
        }
    }
}
