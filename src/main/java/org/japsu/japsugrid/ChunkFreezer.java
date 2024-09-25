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

    public ChunkFreezer() {
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
        return chunk.getInhabitedTime() < 60L || eventDisabledChunks.containsKey(ChunkHelper.getChunkKeyString(chunk));
    }

    private void cleanOldChunks(){

        // Select all chunks that are "old" enough.
        ArrayList<String> oldChunks = new ArrayList<>();
        for (Map.Entry<String, Long> set : eventDisabledChunks.entrySet()) {

            if(System.currentTimeMillis() - set.getValue() >= 5000){
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
}
