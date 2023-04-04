package org.japsu.japsugrid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
                frame.getItem();

                // Get the item frame's item, and drop it to the world.
                Item item = event.getWorld().dropItem(entity.getLocation(), frame.getItem());

                // Disable item despawning.
                item.setUnlimitedLifetime(true);
                // Disable gravity to let item float.
                item.setGravity(false);

                // Clean up the frame.
                frame.remove();
            }
        }
    }
}
