package work.lclpnet.mcct.transform;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MCCT {

    private static final List<IChunkTransformer> pipeline = new ArrayList<>();

    public static void registerTransformer(IChunkTransformer transformer) {
        pipeline.add(Objects.requireNonNull(transformer));
    }

    public static void registerTransformerBefore(IChunkTransformer transformer, IChunkTransformer successor) {
        Objects.requireNonNull(transformer);

        if (successor == null) {
            registerTransformer(transformer);
            return;
        }

        int idx = pipeline.indexOf(successor);
        if (idx == -1) throw new IllegalArgumentException("Successor not registered.");

        pipeline.add(idx, transformer);
    }

    /**
     * Transforms chunk data by applying transformations from registered transformers.
     * @param compound The chunk data to transform.
     * @return True, if the chunk data was modified.
     */
    public static boolean transformChunkNbt(NbtCompound compound) {
        ChunkTransformContext ctx = new ChunkTransformContext(compound);
        pipeline.forEach(transformer -> transformer.transform(ctx));
        return ctx.isDirty();
    }
}
