package org.japsu.japsugrid.gridgenerators;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.japsu.japsugrid.JapsuGrid;
import org.japsu.japsugrid.chunkgenerators.empty.EmptyChunkGenerator;
import org.japsu.japsugrid.chunkgenerators.normal.DefaultChunkGenerator;
import org.japsu.japsugrid.chunkswap.ChunkTransferService;

import java.util.List;

public class AfterStructuresGridGenerator extends GridGenerator implements Listener {

    private static final String SISTER_WORLD_PREFIX = "_source";
    private final int blockSpacingInterval;
    private final boolean removeAllBedrock;
    private final List<Material> nonReplaceableMaterials;
    private ChunkTransferService chunkSwapper;

    public AfterStructuresGridGenerator(
            int blockSpacingInterval,
            boolean removeAllBedrock,
            List<Material> nonReplaceableMaterials,
            boolean disableEventsInNewChunks) {

        super(new EmptyChunkGenerator());

        this.blockSpacingInterval = blockSpacingInterval;
        this.removeAllBedrock = removeAllBedrock;
        this.nonReplaceableMaterials = nonReplaceableMaterials;

        chunkSwapper = new ChunkTransferService(JapsuGrid.PluginLogger);

        JapsuGrid.Singleton.RegisterEventListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInitialized(WorldInitEvent event){

        // Skip sister worlds.
        if(isSisterWorld(event.getWorld()))
            return;

        // Generate a new "sister" world to use as a source for chunk data.
        createOrLoadSisterWorld(event.getWorld());
    }

    /**
     * Called when a chunk loads.
     * We get the sister chunk of the loaded chunk.
     * We load, or generate the sister chunk (asynchronously) if required.
     * We copy the sister chunk's data.
     * We "gridify" the sister's data chunk.
     * We apply sister chunk's data to loaded chunk.
     * We reload the loaded chunk.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoaded(ChunkLoadEvent event){

        // Skip old chunks.
        if(!event.isNewChunk())
            return;

        // Skip sister worlds.
        if(isSisterWorld(event.getWorld()))
            return;

        if(event.getChunk().getX() > 1 || event.getChunk().getX() < -1 || event.getChunk().getZ() > 1 || event.getChunk().getZ() < -1)
            return;

        World world = event.getWorld();
        World sisterWorld = getSisterWorld(world);

        Chunk chunk = event.getChunk();
        Chunk sisterChunk = sisterWorld.getChunkAt(chunk.getX(), chunk.getZ());

        chunkSwapper.transferChunkTo(chunk, sisterChunk);

        //TODO: Fetch data from sister world.
        //TODO: Load the chunk with this, and when the sisterChunk is loaded, transfer the data. Chunk sisterChunk = sisterWorld.loadChunk(chunk.getX(), chunk.getZ());

        //TODO: Generate grid for the loaded chunk. - Execute ChunkSwap operation.
    }

    private World getSisterWorld(World world){
        return Bukkit.getWorld(getSisterWorldName(world));
    }

    private String getSisterWorldName(World world){
        return world.getName() + SISTER_WORLD_PREFIX;
    }

    private boolean isSisterWorld(World world){
        return world.getName().contains(SISTER_WORLD_PREFIX);
    }

    private void createOrLoadSisterWorld(World world){

        WorldCreator sister = new WorldCreator(world.getName() + SISTER_WORLD_PREFIX);
        sister.copy(world).generateStructures(true).generator(new DefaultChunkGenerator());
        sister.createWorld();
    }

    /*
    private final Queue<Chunk> chunkQueue = new LinkedList<>();
    private BukkitTask task;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoaded(ChunkLoadEvent event){
        if(!event.isNewChunk()) return;

        // Add to queue
        chunkQueue.add(event.getChunk());

        if (task == null || task.isCancelled())
        {
            task = Bukkit.getScheduler().runTaskTimer(JapsuGrid.Singleton, () -> processChunk(event.getWorld()), 0L, 1L);
        }
    }

    private void processChunk(World world)
    {
        if (!this.chunkQueue.isEmpty())
        {
            Chunk chunk = this.chunkQueue.poll();

            regenerator.regenerateChunk(chunk);

            if (this.plugin.getSettings().isLogCleanSuperFlatChunks())
            {
                this.plugin.log("Regenerating superflat chunk in " + world.getName() +
                        " at (" + chunk.getX() + ", " + chunk.getZ() + ") " +
                        "(" + this.chunkQueue.size() + " chunk(s) remaining in the queue)");
            }

            //TODO: Unload and reload the chunk after modifications.
        }
        else
        {
            this.task.cancel();
        }
    }*/

    /*public void hackCraftBukkit(World world) {

        CraftWorld craftWorld = (CraftWorld) world;
        net.minecraft.server.level.ServerLevel serverLevel = craftWorld.getHandle();

        net.minecraft.world.level.chunk.ChunkGenerator cachedGen = serverLevel.getChunkSource().chunkMap.generator;

        JapsuGrid.PluginLogger.warning("Doing naughty things with " + serverLevel + "... (͡°͜ʖ͡°)");

        // TODO: Override WorldDimension constructor
        // TODO: Override WorldServer -> ChunkGenerator chunkgenerator = worlddimension.generator();
        // Intercept generation with custom generator.
        AfterStructuresGridChunkGenerator gen = new AfterStructuresGridChunkGenerator(cachedGen, blockSpacingInterval, removeAllBedrock, nonReplaceableMaterials);
        gen.conf = serverLevel.spigotConfig;
        serverLevel.getChunkSource().chunkMap.generator = gen;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInitEvent(WorldInitEvent event){
        hackCraftBukkit(event.getWorld());
    }*/
}
