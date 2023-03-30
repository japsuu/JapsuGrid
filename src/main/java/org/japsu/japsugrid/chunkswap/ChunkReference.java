package org.japsu.japsugrid.chunkswap;


import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkReference {

    public final String WorldName;
    public final int X;
    public final int Z;

    // public ChunkReference(String worldName, int x, int z) {
    //     this.WorldName = worldName;
    //     this.X = x;
    //     this.Z = z;
    // }

    public ChunkReference(Chunk chunk) {
        this.WorldName = chunk.getWorld().getName();
        this.X = chunk.getX();
        this.Z = chunk.getZ();
    }

    public World getWorldReference(){
        return Bukkit.getWorld(WorldName);
    }
}
