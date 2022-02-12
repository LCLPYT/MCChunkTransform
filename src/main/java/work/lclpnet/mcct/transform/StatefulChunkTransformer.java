package work.lclpnet.mcct.transform;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class StatefulChunkTransformer<State> extends ChunkTransformer {

    protected State state;

    protected StatefulChunkTransformer(Predicate<RegistryKey<World>> dimensionTarget,
                                       Predicate<WorldTransformer.RegionFileLocation> regionTarget,
                                       BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget,
                                       Set<IChunkTransformation> transformations,
                                       @Nullable Runnable onComplete,
                                       @Nullable State initialState) {
        super(dimensionTarget, regionTarget, chunkTarget, transformations, onComplete);
        this.state = initialState;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static class Builder<State> {
        private Predicate<RegistryKey<World>> dimensionTarget = dimension -> true;
        private Predicate<WorldTransformer.RegionFileLocation> regionTarget = region -> true;
        private BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget = (chunk, region) -> true;
        private final Set<IChunkTransformation> transformations = new HashSet<>();
        private Runnable onComplete = null;
        private State initialState = null;

        public Builder<State> targetDimensions(Predicate<RegistryKey<World>> dimensionTarget) {
            this.dimensionTarget = Objects.requireNonNull(dimensionTarget);
            return this;
        }

        public Builder<State> targetRegions(Predicate<WorldTransformer.RegionFileLocation> regionTarget) {
            this.regionTarget = Objects.requireNonNull(regionTarget);
            return this;
        }

        public Builder<State> targetChunks(BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget) {
            this.chunkTarget = Objects.requireNonNull(chunkTarget);
            return this;
        }

        public Builder<State> afterComplete(Runnable onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public Builder<State> addTransformation(IChunkTransformation transformation) {
            this.transformations.add(Objects.requireNonNull(transformation));
            return this;
        }

        public Builder<State> withInitialState(State state) {
            this.initialState = state;
            return this;
        }

        public StatefulChunkTransformer<State> create() {
            return new StatefulChunkTransformer<>(dimensionTarget, regionTarget, chunkTarget, transformations, onComplete, initialState);
        }
    }
}
