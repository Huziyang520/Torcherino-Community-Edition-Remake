package com.sci.torcherino.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Loaded chunk access for the acceleration scan.
 * <p>The scan only ever needs chunks that are already there and finished: vanilla does not
 * tick blocks of an unloaded chunk either, so skipping them changes nothing that can be
 * observed - while forcing the load can stall the server for seconds and keep the chunk
 * from ever unloading.</p>
 * <p>{@code getChunkNow} is the strictly non blocking door into the chunk source: it answers
 * {@code null} for a chunk that does not exist yet, and it also refuses chunks that are
 * present but still being created at worldgen time. {@code hasChunk} followed by
 * {@code getChunk} was not enough: a chunk can be present in the source while it is still
 * being built, and that call blocks the server thread until the chunk is ready.</p>
 */
public final class ChunkView {

    private ChunkView() {
    }

    /**
     * @return the chunk, or {@code null} when it is not ready right now. Never loads one.
     */
    public static LevelChunk loadedChunk(ServerLevel level, int chunkX, int chunkZ) {
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }
}
