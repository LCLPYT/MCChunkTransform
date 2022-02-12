package work.lclpnet.mcct.transform;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ChunkTransformer {

    protected final Predicate<RegistryKey<World>> dimensionTarget;
    protected final Predicate<WorldTransformer.RegionFileLocation> regionTarget;
    protected final BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget;
    @Nullable
    protected final Runnable onComplete;
    protected final Set<IChunkTransformation> transformations;

    protected ChunkTransformer(Predicate<RegistryKey<World>> dimensionTarget,
                            Predicate<WorldTransformer.RegionFileLocation> regionTarget,
                            BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget,
                            Set<IChunkTransformation> transformations,
                            @Nullable Runnable onComplete) {
        this.dimensionTarget = Objects.requireNonNull(dimensionTarget);
        this.regionTarget = Objects.requireNonNull(regionTarget);
        this.chunkTarget = Objects.requireNonNull(chunkTarget);
        this.transformations = Objects.requireNonNull(transformations);
        this.onComplete = onComplete;
    }

    public boolean shouldTransformDimension(RegistryKey<World> dimension) {
        return dimensionTarget.test(dimension);
    }

    public boolean shouldTransformRegion(WorldTransformer.RegionFileLocation region) {
        return regionTarget.test(region);
    }

    public boolean shouldTransformChunk(ChunkPos chunk, WorldTransformer.RegionFileLocation region) {
        return chunkTarget.test(chunk, region);
    }

    public void applyTransformations(ChunkTransformContext ctx) {
        transformations.forEach(transformation -> transformation.transform(ctx));
    }

    public void complete() {
        if (onComplete != null) onComplete.run();
    }

    public static class Builder {
        private Predicate<RegistryKey<World>> dimensionTarget = dimension -> true;
        private Predicate<WorldTransformer.RegionFileLocation> regionTarget = region -> true;
        private BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget = (chunk, region) -> true;
        private Set<IChunkTransformation> transformations = new HashSet<>();
        private Runnable onComplete = null;

        public Builder targetDimensions(Predicate<RegistryKey<World>> dimensionTarget) {
            this.dimensionTarget = Objects.requireNonNull(dimensionTarget);
            return this;
        }

        public Builder targetRegions(Predicate<WorldTransformer.RegionFileLocation> regionTarget) {
            this.regionTarget = Objects.requireNonNull(regionTarget);
            return this;
        }

        public Builder targetChunks(BiPredicate<ChunkPos, WorldTransformer.RegionFileLocation> chunkTarget) {
            this.chunkTarget = Objects.requireNonNull(chunkTarget);
            return this;
        }

        public Builder afterComplete(Runnable onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public Builder addTransformation(IChunkTransformation transformation) {
            this.transformations.add(Objects.requireNonNull(transformation));
            return this;
        }

        public ChunkTransformer create() {
            return new ChunkTransformer(dimensionTarget, regionTarget, chunkTarget, transformations, onComplete);
        }
    }
}
