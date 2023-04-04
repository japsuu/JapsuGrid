package org.japsu.japsugrid;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;
import org.japsu.japsugrid.populators.ChunkPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ChunkGenerator extends org.bukkit.generator.ChunkGenerator {

    private final HashSet<Material> nonReplaceableMaterials;

    public ChunkGenerator() {
        this.nonReplaceableMaterials = new HashSet<>();
        this.nonReplaceableMaterials.addAll(JapsuGrid.nonReplaceableMaterials);
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

        if(JapsuGrid.generationMode != JapsuGrid.GenerationMode.BEFORE_DECORATIONS) return;

        generateGridSpigot(chunkX, chunkZ, chunkData);
    }

    @NotNull
    @Override
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {

        if(JapsuGrid.generationMode != JapsuGrid.GenerationMode.AFTER_DECORATIONS) return super.getDefaultPopulators(world);

        ArrayList<BlockPopulator> pops = new ArrayList<>();
        pops.add(new ChunkPopulator(nonReplaceableMaterials));
        return pops;
    }

    private void generateGridSpigot(int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        int minHeight = chunkData.getMinHeight();
        int maxHeight = chunkData.getMaxHeight();

        // Note: If we wouldn't use world-space coordinates to calculate where the grid should be, we could
        // reduce the amount of loop iterations drastically.
        // I'm fine with sacrificing a bit of performance for both readability and a world space grid.

        int worldXBase = chunkX * 16;
        int worldZBase = chunkZ * 16;

        // Loop the chunk.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = worldXBase + x;
                int worldZ = worldZBase + z;

                for (int y = minHeight; y < maxHeight; y++) {

                    Material currentMaterial = chunkData.getType(x, y, z);

                    if(currentMaterial == Material.AIR)
                        continue;

                    // Delete required blocks.
                    if (worldX % JapsuGrid.blockSpacingInterval != 0 || y % JapsuGrid.blockSpacingInterval != 0 || worldZ % JapsuGrid.blockSpacingInterval != 0) {

                        // Skip blocks found in config.
                        if (nonReplaceableMaterials.contains(currentMaterial)) {
                            continue;
                        }

                        chunkData.setBlock(x, y, z, Material.AIR);

                    } else if (JapsuGrid.removeAllBedrock && currentMaterial == Material.BEDROCK) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                    } else {
                        // Executed only for blocks which are not removed.
                        BlockData blockData = chunkData.getBlockData(x, y, z);

                        // Set leaves persistence.
                        if(JapsuGrid.disableNaturalLeafDecay) {
                            if(blockData instanceof Leaves) {
                                Leaves leaves = (Leaves) blockData;
                                leaves.setPersistent(true);
                                chunkData.setBlock(x, y, z, leaves);
                            }
                        }
                    }
                }
            }
        }
    }


    /*
     * We have the support for generating the grid with NMS, but I'd currently rather have maintainable code than
     * NMS spaghetti-mess.
     * Going to leave the code below though, for now.
     */
    /*
    private final HashSet<net.minecraft.world.level.material.Material> nonReplaceableMaterials;
    private net.minecraft.world.level.material.Material nmsBedrockMaterial;
    private BlockState nmsAirState;

    // Convert provided bukkit materials to NMS materials.
     if(nonReplaceableMaterials != null) {
        for (org.bukkit.Material bukkitMat : nonReplaceableMaterials) {
            this.nonReplaceableMaterials.add(bukkitToNmsMaterial(bukkitMat));
        }
    }

    nmsBedrockMaterial = bukkitToNmsMaterial(org.bukkit.Material.BEDROCK);

    Block nmsAirBlock = CraftMagicNumbers.getBlock(org.bukkit.Material.AIR);
    nmsAirState = nmsAirBlock.defaultBlockState();
    // There's also the possibility to:
    // CraftMagicNumbers.getBlock(Material.AIR, (byte) 0);

    private net.minecraft.world.level.material.Material bukkitToNmsMaterial(org.bukkit.Material bukkitMat){
        // If a better way exists, let me know :).
        Block nmsBedrockBlock = CraftMagicNumbers.getBlock(bukkitMat);
        return nmsBedrockBlock.defaultBlockState().getMaterial();
    }

    private void generateGridNms(int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        CraftChunkData craftChunkData = (CraftChunkData) chunkData;
        ChunkAccess chunkAccess = craftChunkData.getHandle();

        // Precalculate block position base coordinates.
        int worldXBase = chunkX * 16;
        int worldZBase = chunkZ * 16;

        // Loop all chunk sections.
        for (LevelChunkSection section : chunkAccess.getSections()) {

            // Skip empty sections.
            if(section.hasOnlyAir())
                continue;

            // Loop the section.
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {

                    // Calculate horizontal position from base.
                    int worldX = worldXBase + x;
                    int worldZ = worldZBase + z;

                    // Precalculate block position base y coordinate.
                    int worldYBase = section.bottomBlockY();

                    for (int y = 0; y < LevelChunkSection.SECTION_HEIGHT; y++) {

                        // Calculate vertical position from base.
                        int worldY = worldYBase + y;

                        // Some NMS magic...
                        BlockState state = section.getBlockState(x, y, z);
                        net.minecraft.world.level.material.Material material = state.getMaterial();
                        // If we wanted to use Spigot Materials, we could:
                        // Material material = CraftMagicNumbers.getMaterial(state).getItemType();

                        // Skip all air blocks.
                        if(state.isAir())
                            continue;

                        if(x < 2 && y < 2 && z < 2)
                            section.setBlockState(x, y, z, nmsAirState, true);

                        if(true) continue;

                        // Delete blocks in a grid pattern.
                        //if (worldX % blockSpacing != 0) {
                        if (worldX % blockSpacing != 0 || worldY % blockSpacing != 0 || worldZ % blockSpacing != 0) {

                            // Skip blocks found in config.
                            if (nonReplaceableMaterials.contains(material)) {
                                continue;
                            }

                            //JapsuGrid.PluginLogger.info("WX: " + worldX + ", WY: " + worldY + ", WZ: " + worldZ);

                            section.setBlockState(x, y, z, nmsAirState, true);

                            // Remove bedrock if required.
                        } else if (removeAllBedrock && material == nmsBedrockMaterial) {
                            section.setBlockState(x, y, z, nmsAirState, true);
                            //JapsuGrid.PluginLogger.info("Delete bedrock");
                        }
                    }
                }
            }
        }
    }*/
}
