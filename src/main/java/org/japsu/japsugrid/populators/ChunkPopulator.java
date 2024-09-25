package org.japsu.japsugrid.populators;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.japsu.japsugrid.JapsuGrid;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;

public class ChunkPopulator extends BlockPopulator {

    private final HashSet<Material> nonReplaceableMaterials;

    public ChunkPopulator(HashSet<Material> nonReplaceableMaterials) {
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
            int worldX = worldXBase + x;
            for (int z = -buffer; z < 16 + buffer; z++) {
                int worldZ = worldZBase + z;

                // Loop vertically.
                for (int y = minHeight; y < maxHeight; y++) {

                    Material material = limitedRegion.getType(worldX, y, worldZ);

                    // Skip all air blocks, no need to change those.
                    if(material == Material.AIR)
                        continue;

                    // Delete blocks that fall outside the grid pattern.
                    if (worldX % JapsuGrid.blockSpacingInterval != 0 || y % JapsuGrid.blockSpacingInterval != 0 || worldZ % JapsuGrid.blockSpacingInterval != 0) {

                        // Skip deletion for blocks found in config.
                        if (nonReplaceableMaterials.contains(material)) {
                            continue;
                        }

                        limitedRegion.setType(worldX, y, worldZ, Material.AIR);

                    } else if (JapsuGrid.removeAllBedrock && material == Material.BEDROCK) {
                        // Remove bedrock if required.
                        limitedRegion.setType(worldX, y, worldZ, Material.AIR);

                    } else {
                        // Executed only for blocks which are not removed.
                        BlockData blockData = limitedRegion.getBlockData(worldX, y, worldZ);

                        // Set leaves persistence.
                        if(JapsuGrid.disableNaturalLeafDecay) {
                            if(blockData instanceof Leaves) {
                                Leaves leaves = (Leaves) blockData;
                                leaves.setPersistent(true);
                                limitedRegion.setBlockData(worldX, y, worldZ, leaves);
                            }
                        }
                    }
                }
            }
        }
    }
}
