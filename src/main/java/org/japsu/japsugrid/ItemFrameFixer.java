package org.japsu.japsugrid;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ItemFrameFixer implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoaded(ChunkLoadEvent event){

        // Only in new chunks.
        if(!event.isNewChunk()) return;

        // For all item frames
        for (Entity entity : event.getChunk().getEntities()) {
            if(entity instanceof ItemFrame) {

                ItemFrame frame = ((ItemFrame) entity);
                Location originalLocation = entity.getLocation();

                // Add a new block to support the ItemFrame.
                Location supportingBlockLocation = originalLocation.getBlock().getRelative(frame.getAttachedFace()).getLocation();
                event.getWorld().getBlockAt(supportingBlockLocation).setType(Material.GLASS);
            }
        }
    }
}
