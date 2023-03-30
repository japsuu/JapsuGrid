package org.japsu.japsugrid.chunkswap;

import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoList;
import com.google.common.base.Stopwatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.japsu.japsugrid.JapsuGrid;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ChunkTransferService {

    public static Logger Logger;

    Map<ChunkReference, ChunkReference> awaitingTransfers = new HashMap<>();

    public ChunkTransferService(Logger logger) {
        ChunkTransferService.Logger = logger;
    }

    //TODO: Simplify, by taking in a single set of chunk coordinates as arguments.
    public void transferChunkTo(Chunk sourceChunk, Chunk destinationChunk) {

        // Init required data.

        ChunkReference fromReference = new ChunkReference(sourceChunk);
        ChunkReference toReference = new ChunkReference(destinationChunk);

        awaitingTransfers.put(fromReference, toReference);

        if(awaitingTransfers.size() < 20)
            return;

        Map<ChunkReference, ChunkReference> transfers = new HashMap<>(awaitingTransfers);

        awaitingTransfers.clear();

        // Start a thread to execute the task.
        ThreadUtils.runAsync(() -> {
            Logger.info("Starting chunk transfer for " + transfers.size() + " chunks.");
            Stopwatch stopwatch = Stopwatch.createStarted();

            try {
                parallelChunkTransfer(transfers).awaitFinish(60);
            } catch (Exception e) {
                Logger.info("Unable to finish chunk transfer. Error: " + e.getMessage());
                e.printStackTrace();
            }

            stopwatch.stop();
            Logger.info("Finished chunk transfer. Taken " + NumberFormat.getNumberInstance().format(stopwatch.elapsed(TimeUnit.MILLISECONDS)) + "ms");
        }, ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService());
    }

    private TaskInfoList parallelChunkTransfer(Map<ChunkReference, ChunkReference> chunksToTransfer) {

        // Fetch the worlds
        // for (Map.Entry<ChunkReference, ChunkReference> swap : chunksToSwap.entrySet()) {
        //     swap.getKey().GenerateWorldReference();
        //     swap.getValue().GenerateWorldReference();
        // }

        // Create the tasks.
        TaskInfoList taskInfoList = ThreadUtils.runForLoopAsync(chunksToTransfer.keySet(), (ChunkReference fromReference) -> {
            ChunkReference toReference = chunksToTransfer.get(fromReference);

            // Get the worlds
            World fromWorld = fromReference.getWorldReference();
            World toWorld = toReference.getWorldReference();

            // Get the chunks
            Chunk fromChunk = fromWorld.getChunkAt(fromReference.X, fromReference.Z);
            Chunk toChunk = toWorld.getChunkAt(toReference.X, toReference.Z);

            transferBlocksOfChunks(fromWorld, toWorld, fromChunk, toChunk);

            return null;
        });

        // Run the tasks.
        taskInfoList.runThreads(ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService());

        return taskInfoList;
    }

    private void transferBlocksOfChunks(World fromWorld, World toWorld, Chunk fromChunk, Chunk toChunk) {

        Future<Void> future = Bukkit.getServer().getScheduler().callSyncMethod(JapsuGrid.Singleton, () -> {
            fromWorld.loadChunk(fromChunk);
            toWorld.loadChunk(toChunk);
            Logger.info(fromWorld.getName() + " loaded " + fromChunk.getX() + ":" + fromChunk.getZ() + " -> " + toWorld.getName() + " " + toChunk.getX() + ":" + toChunk.getZ());
            return null;
        });

        while (!future.isDone()) {
            try {
                //TODO: Implement busy-spinning
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        int minWorldHeight = fromWorld.getMinHeight();
        int maxWorldHeight = fromWorld.getMaxHeight();
        List<Runnable> tasks = Collections.synchronizedList(new ArrayList<>());
        //Map<Pair<Block, BlockData>, Pair<Block, BlockData>> blockMap = new HashMap<>();

        for (int xx = 0; xx < 16; xx++) {
            final int x = xx;

            for (int zz = 0; zz < 16; zz++) {
                final int z = zz;

                // Create a task for each "column" of blocks. 256 in total.
                Runnable task = new Runnable() {

                    @Override
                    public void run() {

                        // Used for optimization.
                        // This number is the highest y value with a non-air block.
                        // Once it is reached, the server skips the rest since they are air blocks.
                        //////int maxHeight = -9999;

                        //TODO: Optimize.
                        // Loop the vertical layer.
                        for (int y = minWorldHeight; y < maxWorldHeight/* && (maxHeight >= y || maxHeight == -9999)*/; y++) {

                            Block fromBlock = fromChunk.getBlock(x, y, z);
                            //Block toBlock = toChunk.getBlock(x, y, z);

                            //BlockData fromBlockData = fromBlock.getBlockData();
                            //BlockData toBlockData = toBlock.getBlockData();

                            // If both blocks are air, swapping them is unnecessary.
                            //if (sourceBlockData.getMaterial() == Material.AIR && destinationBlockData.getMaterial() == Material.AIR)
                            //    continue;

                            // Once the highest block is reached, skip the rest since it is air
                            //////if (maxHeight == -9999)
                            //////    maxHeight = fromWorld.getHighestBlockYAt(fromBlock.getX(), fromBlock.getZ());

                            //Pair<Block, BlockData> sourcePair = new Pair<>(sourceBlock, sourceBlockData);
                            //Pair<Block, BlockData> destinationPair = new Pair<>(destinationBlock, destinationBlockData);

                            //blockMap.put(sourcePair, destinationPair);


                            Bukkit.getServer().getScheduler().callSyncMethod(JapsuGrid.Singleton, () -> {

                                //Logger.info(fromWorld.getName() + " transferring " + locationToString(fromBlock.getLocation()) + " to " + toWorld.getName() + " " + locationToString(toBlock.getLocation()));

                                setBlockInNativeDataPalette(toWorld, fromChunk.getX(), fromChunk.getZ(), fromBlock, false);

                                // fromBlock.setBlockData(toBlockData);
                                // toBlock.setBlockData(fromBlockData);

                                return null;
                            });


                        }
                        //Logger.info("Finished Y on " + x + ":" + z + ".");
                        tasks.remove(this);
                    }
                };

                tasks.add(task);

                //Logger.info("Finished Z on " + x);
            }
            //Logger.info("Finished X");
        }

        TaskInfoList taskInfoList = ThreadUtils.runAsyncList(tasks);

        ExecutorService executorService = Executors.newCachedThreadPool();
        taskInfoList.runThreads(executorService);

        taskInfoList.awaitFinish(1);

        //Logger.info("Finished waiting for chunk tasks");

        Bukkit.getServer().getScheduler().callSyncMethod(JapsuGrid.Singleton, () -> {
            fromWorld.unloadChunk(fromChunk);
            toWorld.unloadChunk(toChunk);
            Logger.info(fromWorld.getName() + " unloaded " + fromChunk.getX() + ":" + fromChunk.getZ() + " and " + toWorld.getName() + " unloaded " + toChunk.getX() + ":" + toChunk.getZ());
            return null;
        });
    }

    private static String locationToString(Location loc) {
        return "x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ();
    }

    //TODO: Find out what's the problem. Setting new blocks doesn't current do anything.
    public static void setBlockInNativeDataPalette(World world, int chunkX, int chunkZ, Block block, boolean applyPhysics) {

        final ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        final LevelChunk levelChunk = serverLevel.getChunk(chunkX, chunkZ);
        // If using world position, bitshift to get chunk position: final LevelChunk levelChunk = serverLevel.getChunk(x >> 4, z >> 4);

        final CraftBlock craftBlock = (CraftBlock) block;
        final net.minecraft.core.BlockPos bp = craftBlock.getPosition();

        //int id = net.minecraft.world.level.block.Block.getId((BlockState) block.getState());

        // Get block ID and data.
        //int id = net.minecraft.world.level.block.Block.getId(craftBlock.getNMS());
        //byte data = craftBlock.getData();

        //final int combined = id + (data << 12);
        //net.minecraft.world.level.block.Block.getId()
        //final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

        //serverLevel.setBlock(bp, craftBlock.getNMS(), 1);
        levelChunk.setBlockState(bp, craftBlock.getNMS(), applyPhysics);
    }
}
