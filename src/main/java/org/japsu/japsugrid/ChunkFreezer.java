package org.japsu.japsugrid;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.japsu.japsugrid.helpers.ChunkHelper;

import java.util.HashMap;

/**
 * Stops all physics events (liquid flow, falling blocks) from happening in the first few seconds of a chunk's lifetime.
 */
public class ChunkFreezer implements Listener {

    private final HashMap<Long, Long> eventDisabledChunks;
    BukkitScheduler scheduler;

    public ChunkFreezer(){

        eventDisabledChunks = new HashMap<>();
        scheduler = Bukkit.getServer().getScheduler();
    }

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event){

        // Only cache chunks that are loaded for the first time, because we only want to "freeze" them initially.
        if(event.isNewChunk()) {
            Chunk chunk = event.getChunk();
            long chunkKey = ChunkHelper.getChunkKey(chunk);
            eventDisabledChunks.put(chunkKey, System.currentTimeMillis());

            // Check for event eligibility again every second.
            scheduleEventCheck(chunk, chunkKey);
        }
    }

    private void scheduleEventCheck(Chunk chunk, long chunkKey) {
        scheduler.runTaskLater(JapsuGrid.Singleton, () -> checkChunkEventEligibility(chunk, chunkKey), 20L);
    }

    private void checkChunkEventEligibility(Chunk chunk, long chunkKey) {

        // Calculate for how long the chunk has existed in the world.
        long chunkExistedForMillis = System.currentTimeMillis() - eventDisabledChunks.get(chunkKey);

        // Check that either the chunk has been alive for long enough, or player has been near it for long enough.
        // The "existed for" check is really only for server initialization/load, since it seems like Minecraft really
        // wants to get those liquids flowing...
        //
        // Feels like a hack, but...
        // Don't fix it if it ain't broken!
        // I still have a very nasty feeling that this will cause problems later...
        if(chunk == null || chunkExistedForMillis >= 10000L || chunk.getInhabitedTime() > 60){
            eventDisabledChunks.remove(chunkKey);
        } else {
            scheduleEventCheck(chunk, chunkKey);
        }
    }

    private boolean chunkHasEventsDisabled(Chunk fromChunk){

        // Check that the chunk doesn't have events disabled.
        return eventDisabledChunks.containsKey(ChunkHelper.getChunkKey(fromChunk));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event){

        // Get the event chunks.
        Chunk fromChunk = event.getSourceBlock().getChunk();
        Chunk toChunk = event.getBlock().getChunk();

        // Cancel if chunk isn't eligible for events.
        if (chunkHasEventsDisabled(fromChunk) || chunkHasEventsDisabled(toChunk))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromToEvent(BlockFromToEvent event){

        // Get the event chunks.
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        // Cancel if chunk isn't eligible for events.
        if (chunkHasEventsDisabled(fromChunk) || chunkHasEventsDisabled(toChunk))
            event.setCancelled(true);
    }
}
