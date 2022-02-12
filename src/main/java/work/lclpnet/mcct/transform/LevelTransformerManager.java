package work.lclpnet.mcct.transform;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class LevelTransformerManager implements ILevelTransformerManager {

    @Override
    public void complete() {
        MCCT.getChunkTransformers()
                .forEachOrdered(ChunkTransformer::complete);
    }

    @Override
    public boolean shouldTransformDimension(RegistryKey<World> dimension) {
        return MCCT.getChunkTransformers()
                .anyMatch(transformer -> transformer.shouldTransformDimension(dimension));
    }

    @Override
    public boolean shouldTransformRegion(WorldTransformer.RegionFileLocation region) {
        return MCCT.getChunkTransformers()
                .anyMatch(transformer -> transformer.shouldTransformRegion(region));
    }

    @Override
    public boolean shouldTransformChunk(ChunkPos chunkPos, WorldTransformer.RegionFileLocation region) {
        return MCCT.getChunkTransformers()
                .anyMatch(transformer -> transformer.shouldTransformChunk(chunkPos, region));
    }

    @Override
    public boolean transformChunk(NbtCompound chunkTag, ChunkPos chunkPos, WorldTransformer.RegionFileLocation region) {
        return MCCT.transformChunkNbt(chunkTag, chunkPos, region);
    }
}
