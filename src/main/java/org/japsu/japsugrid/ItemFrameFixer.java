package org.japsu.japsugrid;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ItemFrameFixer implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoaded(ChunkLoadEvent event){

        for (Entity entity : event.getChunk().getEntities()) {

            if(entity instanceof ItemFrame){

                ItemFrame frame = ((ItemFrame) entity);
                frame.setFixed(true);
            }
        }
    }
}
