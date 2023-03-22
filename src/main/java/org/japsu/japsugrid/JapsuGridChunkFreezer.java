package org.japsu.japsugrid;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Stops all physics events (liquid flow, falling blocks) from happening in the first few seconds of a chunk's lifetime.
 */
public class JapsuGridChunkFreezer implements Listener {

    private final HashMap<Long, Long> eventDisabledChunks;

    public JapsuGridChunkFreezer(Plugin plugin){

        eventDisabledChunks = new HashMap<>();

        // Clean up the HashMap every 10 seconds.
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::CleanOldChunks, 100, 100);
    }

    /**
     * Enables events for chunks older than 3 seconds.
     */
    private void CleanOldChunks(){

        // Select all chunks that are "old" enough.
        ArrayList<Long> oldChunks = new ArrayList<>();
        for (Map.Entry<Long, Long> set : eventDisabledChunks.entrySet()) {
            if(System.currentTimeMillis() - set.getValue() >= 10000){
                oldChunks.add(set.getKey());
            }
        }

        // Enable events for those chunks.
        for(Long key : oldChunks){
            eventDisabledChunks.remove(key);
        }
    }

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event){

        // Only cache chunks that are loaded for the first time, because we only want to "freeze" them initially.
        if(event.isNewChunk()){
            eventDisabledChunks.put(event.getChunk().getChunkKey(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event){

        // Get the event chunks.
        Chunk fromChunk = event.getSourceBlock().getChunk();
        Chunk toChunk = event.getBlock().getChunk();

        // Return early if chunk isn't eligible for events.
        if(isChunkEligibleForEvents(fromChunk) && isChunkEligibleForEvents(toChunk)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromToEvent(BlockFromToEvent event){

        // Get the event chunks.
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        // Return early if chunk isn't eligible for events.
        if(isChunkEligibleForEvents(fromChunk) && isChunkEligibleForEvents(toChunk)) return;
        event.setCancelled(true);
    }

    private boolean isChunkEligibleForEvents(Chunk fromChunk){

        // Check that the chunk has been inhabited by players, and doesn't have events disabled
        return (fromChunk.getInhabitedTime() >= 40 && !eventDisabledChunks.containsKey(fromChunk.getChunkKey()));
    }
}
