package org.japsu.japsugrid.chunkgenerators.grid;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.material.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.generator.CraftChunkData;
import org.bukkit.craftbukkit.v1_19_R1.generator.InternalChunkGenerator;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// public class AfterStructuresGridChunkGenerator extends CustomChunkGenerator {
public class AfterStructuresGridChunkGenerator extends InternalChunkGenerator {

    private final ChunkGenerator delegate;
    private int blockSpacingInterval;
    private HashSet<org.bukkit.Material> nonReplaceableMaterials;
    private boolean removeAllBedrock;
    private Material nmsBedrockMaterial;
    private BlockState nmsAirState;

    public AfterStructuresGridChunkGenerator(ChunkGenerator delegate, int blockSpacingInterval, boolean removeAllBedrock, List<org.bukkit.Material> nonReplaceableMaterials) {

        super(delegate.structureSets, delegate.structureOverrides, delegate.getBiomeSource());
        //super(world, nmsGenerator, bukkitGenerator);

        this.delegate = delegate;
        this.blockSpacingInterval = blockSpacingInterval;
        this.removeAllBedrock = removeAllBedrock;
        this.nonReplaceableMaterials = new HashSet<>();

        // Convert provided bukkit materials to NMS materials.
        if(nonReplaceableMaterials != null) {
            this.nonReplaceableMaterials.addAll(nonReplaceableMaterials);
            //for (org.bukkit.Material bukkitMat : nonReplaceableMaterials) {
            //    this.nonReplaceableMaterials.add(bukkitToNmsMaterial(bukkitMat));
            //}
        }

        nmsBedrockMaterial = bukkitToNmsMaterial(org.bukkit.Material.BEDROCK);

        Block nmsAirBlock = CraftMagicNumbers.getBlock(org.bukkit.Material.AIR);
        nmsAirState = nmsAirBlock.defaultBlockState();
    }

    private Material bukkitToNmsMaterial(org.bukkit.Material bukkitMat){
        // If a better way exists, let me know :).
        Block nmsBedrockBlock = CraftMagicNumbers.getBlock(bukkitMat);
        return nmsBedrockBlock.defaultBlockState().getMaterial();
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return Codec.unit(null);
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        delegate.applyCarvers(worldGenRegion, l, randomState, biomeManager, structureManager, chunkAccess, carving);
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
        delegate.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        delegate.spawnOriginalMobs(worldGenRegion);
    }

    @Override
    public int getGenDepth() {
        return delegate.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return delegate.fillFromNoise(executor, blender, randomState, structureManager, chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return delegate.getMinY();
    }

    @Override
    public int getBaseHeight(int i, int i1, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return delegate.getBaseHeight(i, i1, types, levelHeightAccessor, randomState);
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return delegate.getBaseColumn(i, j, levelheightaccessor, randomstate);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        delegate.addDebugScreenInfo(list, randomState, blockPos);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
        return delegate.getSpawnHeight(levelheightaccessor);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel generatoraccessseed, ChunkAccess ichunkaccess, StructureManager structuremanager, boolean vanilla) {
        super.applyBiomeDecoration(generatoraccessseed, ichunkaccess, structuremanager, vanilla);

        generateGrid(generatoraccessseed, ichunkaccess);
    }

    private void generateGrid(WorldGenLevel level, ChunkAccess chunk){

        /*LevelChunkSection[] sections = chunk.getSections();

        // Loop all sections of the chunk
        for (LevelChunkSection section : sections) {

            if (section.hasOnlyAir()) continue;

            int worldX;
            int worldZ;
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                for (int y = 0; y < LevelChunkSection.SECTION_HEIGHT; y++) {
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {

                        worldX = chunk.getPos().x * 16 + x;
                        worldZ = chunk.getPos().z * 16 + z;

                        BlockState state = section.getBlockState(x, y, z);
                        Material material = state.getMaterial();

                        if(material == Material.AIR)
                            continue;

                        // Delete required blocks.
                        if (worldX % blockSpacingInterval != 0 || y % blockSpacingInterval != 0 || worldZ % blockSpacingInterval != 0) {

                            // Skip blocks found in config.
                            if (nonReplaceableMaterials.contains(material)) {
                                continue;
                            }

                            section.setBlockState(x, y, z, nmsAirState, true);

                            // Replace bedrock if required.
                        } else if (removeAllBedrock && material == nmsBedrockMaterial) {

                            section.setBlockState(x, y, z, nmsAirState, false);
                        }
                    }
                }
            }
        }*/


        //LevelChunk lc = (LevelChunk)chunk;
        //Chunk bukkitChunk = chunk.getBukkitChunk();
        //Block bukkitBlock = bukkitChunk.getBlock(x, y, z);

        // NOTE: Won't work, because features may be placed across multiple chunks.
        // This means that if a chunk has been "gridded" already, it might still
        // receive feature placements from its neighbouring chunks.

        CraftWorld world = level.getMinecraftWorld().getWorld();
        CraftChunkData chunkData = new CraftChunkData(world, chunk);

        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();

        // Generate the grid pattern.
        for (int y = minHeight; y < maxHeight; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    int worldX = chunk.getPos().x * 16 + x;
                    int worldZ = chunk.getPos().z * 16 + z;

                    org.bukkit.Material currentMaterial = chunkData.getType(x, y, z);

                    //NOTE: TODO: Blocks.AIR.defaultBlockState()

                    if(currentMaterial == org.bukkit.Material.AIR)
                        continue;

                    // Delete required blocks.
                    if (worldX % blockSpacingInterval != 0 || y % blockSpacingInterval != 0 || worldZ % blockSpacingInterval != 0) {

                        // Skip blocks found in config.
                        if (nonReplaceableMaterials.contains(currentMaterial)) {
                            continue;
                        }

                        chunkData.setBlock(x, y, z, org.bukkit.Material.AIR);


                        // BlockData blockData = Bukkit.createBlockData(org.bukkit.Material.AIR);
                        // Stolen from CraftBlockData.class
                        // Block block = CraftMagicNumbers.getBlock(org.bukkit.Material.AIR);
                        // BlockState newState = block.defaultBlockState();



                        // Ignore this pls.
                        // CraftBlockData data = CraftBlockData.newData(org.bukkit.Material.AIR, null);
                        // BlockState newState = data.getState();

                    } else if (removeAllBedrock && currentMaterial == org.bukkit.Material.BEDROCK) {
                        chunkData.setBlock(x, y, z, org.bukkit.Material.AIR);
                    }
                }
            }
        }
    }
}