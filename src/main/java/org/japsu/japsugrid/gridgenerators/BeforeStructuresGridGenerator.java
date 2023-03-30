package org.japsu.japsugrid.gridgenerators;

import org.bukkit.Material;
import org.japsu.japsugrid.ChunkFreezer;
import org.japsu.japsugrid.chunkgenerators.grid.BeforeStructuresGridChunkGenerator;

import java.util.List;

public class BeforeStructuresGridGenerator extends GridGenerator {

    public BeforeStructuresGridGenerator(int blockSpacingInterval, boolean removeAllBedrock, List<Material> nonReplaceableMaterials, boolean disableEventsInNewChunks) {

        super(new BeforeStructuresGridChunkGenerator(blockSpacingInterval, removeAllBedrock, nonReplaceableMaterials));

        if(disableEventsInNewChunks)
            new ChunkFreezer();
    }
}
