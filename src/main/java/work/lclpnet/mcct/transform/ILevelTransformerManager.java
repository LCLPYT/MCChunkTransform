package work.lclpnet.mcct.transform;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface ILevelTransformerManager {

    void complete();

    boolean shouldTransformDimension(RegistryKey<World> dimension);

    boolean shouldTransformRegion(WorldTransformer.RegionFileLocation region);

    boolean shouldTransformChunk(ChunkPos chunkPos, WorldTransformer.RegionFileLocation region);

    boolean transformChunk(NbtCompound chunkTag, ChunkPos chunkPos, WorldTransformer.RegionFileLocation region);
}
