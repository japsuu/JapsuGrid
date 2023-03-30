package org.japsu.japsugrid;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class Helpers {
    public static long getChunkKey(Chunk chunk) {
        return getChunkKey(chunk.getX(), chunk.getZ());
    }

    private static long getChunkKey(@NotNull Location loc) {
        return getChunkKey((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4);
    }

    private static long getChunkKey(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }
}
