package work.lclpnet.mcct.transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.SaveLoader;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldTransformer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");

    public final LevelInfo levelInfo;
    public final LevelStorage.Session session;
    protected final ImmutableSet<RegistryKey<World>> worlds;
    protected final ProgressListener progressListener;
    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected ILevelTransformerManager transformer = null;

    protected WorldTransformer(LevelStorage.Session session, LevelInfo levelInfo, ImmutableSet<RegistryKey<World>> worlds, ProgressListener progressListener) {
        this.session = Objects.requireNonNull(session);
        this.levelInfo = Objects.requireNonNull(levelInfo);
        this.worlds = Objects.requireNonNull(worlds);
        this.progressListener = Objects.requireNonNull(progressListener);
    }

    public static CompletableFuture<WorldTransformer> create(MinecraftClient client, LevelStorage.Session storageSession, ProgressListener progressListener) {
        return CompletableFuture.supplyAsync(() -> createSync(client, storageSession, progressListener));
    }

    protected static WorldTransformer createSync(MinecraftClient client, LevelStorage.Session storageSession, ProgressListener progressListener) {
        try (SaveLoader saveLoader = client.createIntegratedServerLoader().createSaveLoader(storageSession, false)) {

            SaveProperties saveProperties = saveLoader.saveProperties();
            storageSession.backupLevelDataFile(saveLoader.dynamicRegistryManager(), saveProperties);
            var immutableSet = saveProperties.getGeneratorOptions().getWorlds();

            return new WorldTransformer(storageSession, saveProperties.getLevelInfo(), immutableSet, progressListener);
        } catch (Exception e) {
            LOGGER.warn("Failed to load datapacks, can't optimize world", e);
            return null;
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public CompletableFuture<Void> transform() {
        return CompletableFuture.runAsync(this::transformSync);
    }

    protected void transformSync() {
        if (running.get()) throw new IllegalStateException("Transform is already running");
        running.set(true);

        transformer = MCCT.createTransformer(this);

        final ImmutableList<RegistryKey<World>> list = this.worlds.asList();

        final int unfilteredDimCount = worlds.size();
        LOGGER.info("Found {} dimensions", unfilteredDimCount);

        final List<RegistryKey<World>> transformDimensions = list.stream()
                .filter(transformer::shouldTransformDimension).toList();

        final int dimCount = transformDimensions.size();
        final int ignoredDimensions = unfilteredDimCount - dimCount;
        if (ignoredDimensions > 0) LOGGER.info("Ignoring {} dimensions", ignoredDimensions);

        this.progressListener.setSteps(dimCount);

        for (int i = 0; i < dimCount; i++) {
            this.progressListener.updateCurrentStep(i + 1);
            this.transformWorld(list.get(i));
        }

        transformer.complete();
        transformer = null;

        LOGGER.info("Transformation complete.");

        running.set(false);
    }

    protected void transformWorld(RegistryKey<World> world) {
        LOGGER.info("Transforming world {}...", world.getValue());

        Path worldPath = this.session.getWorldDirectory(world);
        Path regionPath = worldPath.resolve("region");
        final File regionDirectory = regionPath.toFile();
        File[] files = regionDirectory.listFiles((filex, string) -> string.endsWith(".mca"));
        if (files == null) {
            LOGGER.info("No region files found in {}", regionDirectory.getAbsolutePath());
            return;
        }

        List<RegionFileLocation> regionFiles = new ArrayList<>();

        for (File regionFile : files) {
            Matcher matcher = REGION_FILE_PATTERN.matcher(regionFile.getName());
            if (!matcher.matches()) continue;

            int regionX = Integer.parseInt(matcher.group(1)) << 5;
            int regionY = Integer.parseInt(matcher.group(2)) << 5;

            final RegionFileLocation location = new RegionFileLocation(regionFile.toPath(), regionX, regionY, world);
            if (transformer.shouldTransformRegion(location))
                regionFiles.add(location);
        }

        final int regionFileCount = regionFiles.size();
        LOGGER.info("Found {} region files.", regionFileCount);

        for (int i = 0; i < regionFileCount; i++) {
            RegionFileLocation regionFile = regionFiles.get(i);
            transformRegionFile(regionFile, regionPath);
            this.progressListener.updateProgress((i + 1) / (float) regionFileCount);
        }

        LOGGER.info("World {} transformed successfully.", world.getValue());
    }

    protected void transformRegionFile(RegionFileLocation region, Path regDirectory) {
        LOGGER.info("Transforming region file {}...", region.file.getFileName());

        try (RegionFile regionFile = new RegionFile(region.file, regDirectory, true)) {
            List<ChunkPos> chunkPositions = Lists.newArrayList();

            for (int x = 0; x < 32; ++x) {
                for (int y = 0; y < 32; ++y) {
                    ChunkPos chunkPos = new ChunkPos(x + region.x, y + region.y);

                    if (regionFile.isChunkValid(chunkPos) && transformer.shouldTransformChunk(chunkPos, region))
                        chunkPositions.add(chunkPos);
                }
            }

            for (ChunkPos chunkPosition : chunkPositions) {
                transformChunk(regionFile, chunkPosition, region);
            }
        } catch (Throwable ignored) {
            LOGGER.warn("Could not read {} as region file", region.file.getFileName());
        }
    }

    protected void transformChunk(RegionFile regionFile, ChunkPos chunkPos, RegionFileLocation region) throws IOException {
        NbtCompound chunkTag;
        try (DataInputStream chunkIn = regionFile.getChunkInputStream(chunkPos)) {
            if (chunkIn == null) {
                LOGGER.info("Failed to fetch input stream for chunk {}", chunkPos);
                return;
            }

            chunkTag = NbtIo.read(chunkIn);
        }

        boolean dirty = transformer.transformChunk(chunkTag, chunkPos, region);

        if (dirty) {
            try (DataOutputStream chunkOut = regionFile.getChunkOutputStream(chunkPos)) {
                NbtIo.write(chunkTag, chunkOut);
            }
        }
    }

    public record RegionFileLocation(Path file, int x, int y, RegistryKey<World> world) {}

    public interface ProgressListener {
        void setSteps(int steps);
        void updateCurrentStep(int currentStep);
        void updateProgress(float progress);
    }
}
