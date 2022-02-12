package work.lclpnet.mcct.transform;

/**
 * Stateful transformation interface.
 * <b>Careful!</b> Using this interface can be unsafe. Only use it if you must!
 * @param <State> The state type of the transformation.
 */
public interface IStatefulChunkTransformation<State> extends IChunkTransformation {

    @SuppressWarnings("unchecked")
    @Override
    default void transform(ChunkTransformContext ctx, ChunkTransformer transformer) {
        transformStateful(ctx, (StatefulChunkTransformer<State>) transformer);
    }

    void transformStateful(ChunkTransformContext ctx, StatefulChunkTransformer<State> transformer);
}
