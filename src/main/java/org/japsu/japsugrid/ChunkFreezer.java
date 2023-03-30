package org.japsu.japsugrid;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.japsu.japsugrid.helpers.ChunkHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Stops all physics events (liquid flow, falling blocks) from happening in the first few seconds of a chunk's lifetime.
 */
public class ChunkFreezer implements Listener {

    private final HashMap<String, Long> eventDisabledChunks;

    public ChunkFreezer(){
        eventDisabledChunks = new HashMap<>();

        // Clean up the HashMap every X seconds.
        JapsuGrid.Singleton.ScheduleSyncRepeatingTask(this::cleanOldChunks, 20L * 5L, 20L * 5L);
    }

    private boolean isChunkCurrentlyFrozen(Chunk chunk) {

        // Check that either the chunk has been alive for long enough, or player has been near it for long enough.
        // The "existed for" check is really only for server initialization/load, since it seems like Minecraft really
        // wants to get those liquids flowing...
        //
        // Feels like a hack, but...
        // Don't fix it if it ain't broken!
        // I still have a very nasty feeling that this will cause problems later...
        return chunk.getInhabitedTime() < 40L || eventDisabledChunks.containsKey(ChunkHelper.getChunkKeyString(chunk));
    }

    private void cleanOldChunks(){

        // Select all chunks that are "old" enough.
        ArrayList<String> oldChunks = new ArrayList<>();
        for (Map.Entry<String, Long> set : eventDisabledChunks.entrySet()) {

            if(System.currentTimeMillis() - set.getValue() >= 10000){
                oldChunks.add(set.getKey());
            }
        }

        // Enable events for those chunks.
        for(String key : oldChunks){
            eventDisabledChunks.remove(key);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoaded(ChunkLoadEvent event){

        // Only cache chunks that are loaded for the first time, because we only want to "freeze" them initially.
        if(event.isNewChunk()){
            eventDisabledChunks.put(ChunkHelper.getChunkKeyString(event.getChunk()), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {

        // Get the event chunks.
        Chunk fromChunk = event.getSourceBlock().getChunk();
        Chunk toChunk = event.getBlock().getChunk();

        // Freeze if chunk isn't eligible for events.
        if(isChunkCurrentlyFrozen(fromChunk) || isChunkCurrentlyFrozen(toChunk))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromToEvent(BlockFromToEvent event) {

        // Get the event chunks.
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        // Freeze if chunk isn't eligible for events.
        if(isChunkCurrentlyFrozen(fromChunk) || isChunkCurrentlyFrozen(toChunk))
            event.setCancelled(true);
    }


    /*
    private final HashMap<String, Long> eventDisabledChunks;
    BukkitScheduler scheduler;

    public ChunkFreezer(){

        eventDisabledChunks = new HashMap<>();
        scheduler = Bukkit.getServer().getScheduler();
    }

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event) {

        // Only cache chunks that are loaded for the first time, because we only want to "freeze" them initially.
        if(event.isNewChunk()) {
            Chunk chunk = event.getChunk();
            //long chunkKey = ChunkHelper.getChunkKey(chunk);
            String chunkKey = ChunkHelper.getChunkKeyString(chunk);
            eventDisabledChunks.put(chunkKey, System.currentTimeMillis());

            // Check for event eligibility again every second.
            scheduleEventCheck(chunk, chunkKey);
        }
    }

    private void scheduleEventCheck(Chunk chunk, String chunkKey) {
        scheduler.runTaskLater(JapsuGrid.Singleton, () -> checkChunkEventEligibility(chunk, chunkKey), 20L);
    }

    private void checkChunkEventEligibility(Chunk chunk, String chunkKey) {

        // Calculate for how long the chunk has existed in the world.
        if(!eventDisabledChunks.containsKey(chunkKey)) {
            JapsuGrid.PluginLogger.warning("Tried to unsuccessfully unfreeze chunk with key " + chunkKey + ".");
            return;
        }

        long chunkExistedForMillis = System.currentTimeMillis() - eventDisabledChunks.get(chunkKey);


        if(chunk == null || chunkExistedForMillis >= 10000L || chunk.getInhabitedTime() > 60){
            eventDisabledChunks.remove(chunkKey);
        } else {
            scheduleEventCheck(chunk, chunkKey);
        }
    }

    private boolean chunkHasEventsDisabled(Chunk fromChunk) {

        // Check that the chunk doesn't have events disabled.
        return eventDisabledChunks.containsKey(ChunkHelper.getChunkKeyString(fromChunk));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {

        // Get the event chunks.
        Chunk fromChunk = event.getSourceBlock().getChunk();
        Chunk toChunk = event.getBlock().getChunk();

        // Cancel if chunk isn't eligible for events.
        if (chunkHasEventsDisabled(fromChunk) || chunkHasEventsDisabled(toChunk))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockFromToEvent(BlockFromToEvent event){

        if(event.getBlock().isLiquid())
            event.setCancelled(true);

        // Get the event chunks.
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        // Cancel if chunk isn't eligible for events.
        if (chunkHasEventsDisabled(fromChunk) || chunkHasEventsDisabled(toChunk))
            event.setCancelled(true);
    }*/
}
