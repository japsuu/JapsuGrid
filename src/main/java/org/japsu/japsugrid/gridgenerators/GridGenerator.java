package org.japsu.japsugrid.gridgenerators;

import org.bukkit.generator.ChunkGenerator;

public abstract class GridGenerator {

    private final ChunkGenerator generator;

    public GridGenerator(ChunkGenerator generator){

        this.generator = generator;
    }

    public ChunkGenerator getChunkGeneratorDelegate(){
        return generator;
    }

    public void onEnable(){
    }

    public void onDisable() {
    }
}
