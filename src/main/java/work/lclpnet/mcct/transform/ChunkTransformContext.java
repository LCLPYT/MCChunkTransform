package work.lclpnet.mcct.transform;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

import java.util.Objects;

public class ChunkTransformContext {

    private NbtCompound compound;
    public final ChunkPos chunkPos;
    public final WorldTransformer.RegionFileLocation region;
    private boolean dirty = false;

    ChunkTransformContext(NbtCompound compound, ChunkPos chunkPos, WorldTransformer.RegionFileLocation region) {
        this.compound = Objects.requireNonNull(compound);
        this.chunkPos = chunkPos;
        this.region = region;
    }

    /**
     * Completely replace the chunk data.
     * Automatically marks the chunk dirty.
     *
     * <b>Careful!</b> If you use this method, you could break the world data really easily.
     * @param compound The new chunk data.
     */
    public void setCompound(NbtCompound compound) {
        Objects.requireNonNull(compound);
        if (Objects.equals(this.compound, compound)) return;

        this.compound = compound;
        markDirty();
    }

    /**
     * Get the chunk data as {@link NbtCompound}.
     * The data could already be modified by other transformers earlier in the transform pipeline.
     * Otherwise, this data is directly read from disk.
     * @return The chunk data.
     */
    public NbtCompound getCompound() {
        return compound;
    }

    /**
     * Marks the chunk as modified.
     * Dirty marked chunks will be written back to disk, while others are only read.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Checks whether this chunk was modified.
     * @return Whether the chunk was modified.
     */
    public boolean isDirty() {
        return dirty;
    }
}
