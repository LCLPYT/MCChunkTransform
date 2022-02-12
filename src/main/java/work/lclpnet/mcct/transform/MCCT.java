package work.lclpnet.mcct.transform;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class MCCT {

    private static Function<WorldTransformer, ILevelTransformerManager> levelTransformProvider = transformer -> new LevelTransformerManager();
    private static final List<ChunkTransformer> transformers = new ArrayList<>();

    public static void setLevelTransformProvider(@Nonnull Function<WorldTransformer, ILevelTransformerManager> levelTransformProvider) {
        MCCT.levelTransformProvider = Objects.requireNonNull(levelTransformProvider);
    }

    static ILevelTransformerManager createTransformer(WorldTransformer transformer) {
        return levelTransformProvider.apply(transformer);
    }

    public static Stream<ChunkTransformer> getChunkTransformers() {
        return transformers.stream();
    }

    public static boolean isTransformerRegistered(ChunkTransformer transformer) {
        Objects.requireNonNull(transformer);
        return transformers.contains(transformer);
    }

    public static void registerTransformer(ChunkTransformer transformer) {
        Objects.requireNonNull(transformer);
        if (transformers.contains(transformer)) throw new IllegalArgumentException("Transformer already registered");
        transformers.add(transformer);
    }

    public static void registerTransformerBefore(ChunkTransformer transformer, ChunkTransformer successor) {
        Objects.requireNonNull(transformer);

        if (successor == null) {
            registerTransformer(transformer);
            return;
        }

        int idx = transformers.indexOf(successor);
        if (idx == -1) throw new IllegalArgumentException("Successor not registered.");

        transformers.add(idx, transformer);
    }

    public static boolean unregisterTransformer(ChunkTransformer transformer) {
        if (transformer != null) return transformers.remove(transformer);
        return false;
    }

    /**
     * Transforms chunk data by applying transformations from registered transformers.
     * @param compound The chunk data to transform.
     * @param chunkPos The position of the chunk.
     * @param region Information about the region the chunk is a part of.
     * @return True, if the chunk data was modified.
     */
    public static boolean transformChunkNbt(NbtCompound compound, ChunkPos chunkPos, WorldTransformer.RegionFileLocation region) {
        ChunkTransformContext ctx = new ChunkTransformContext(compound, chunkPos, region);
        transformers.forEach(transformer -> transformer.applyTransformations(ctx));
        return ctx.isDirty();
    }
}
