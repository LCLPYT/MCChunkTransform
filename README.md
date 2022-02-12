# MCChunkTransform
[![Gradle Publish](https://github.com/LCLPYT/MCChunkTransform/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/LCLPYT/MCChunkTransform/actions/workflows/gradle-publish.yml)

A simple Minecraft chunk transformer API.

## What it does
MCChunkTransform as a simple Fabric client modification for applying transformations to your Minecraft worlds programmatically.
Though the mod is written for Fabric, it can also transform worlds created in Forge, without making them incompatible.

In your singleplayer world selection screen, if you edit the world, you will find a new button:
![Edit world screen](https://raw.githubusercontent.com/LCLPYT/MCChunkTransform/main/img/edit_world.jpg)

Click this button to start the transformation process.
By it's own, this mod does not apply any modifications to your world; it only reads all chunks one by another.

![Transformation progress](https://github.com/LCLPYT/MCChunkTransform/raw/main/img/transform.jpg)

## Create custom transformers
In order to apply custom transformation to your world, you need to register a `ChunkTransformer` in your mod.
If you have no mod yet, you can create one as described in the [Fabric wiki](https://fabricmc.net/wiki/tutorial:introduction).

### Gradle dependency
After you have everything set up, add MCCT (MCChunkTransform) as a Gradle dependency:
```gradle
repositories {
    // add the LCLPNetwork maven repository
    maven {
        url "https://repo.lclpnet.work/repository/internal"
    }
}

dependencies {
    // your other dependencies ...
    modImplementation "work.lclpnet.mods:mcct:<version>"
}
```

You can lookup all available versions [here](https://repo.lclpnet.work/#artifact/work.lclpnet.mods/mcct).

### Implementation
In your ModInitializer or ClientInitializer, register a new transformer by calling `MCCT::registerTransformer`:
```java
public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MCCT.registerTransformer(new ChunkTransformer.Builder().addTransformation(ctx -> {
            CompoundTag chunkData = ctx.getCompound();
            // read and write chunkData here...
            // if you modified the data, remember to call ctx.markDirty() so that your changes get written to disk.
        }).create());
    }
}
```
You can use the `ChunkTransformer.Builder` to configure your chunk transformer.
For the sake of simplicity, we only register a single chunk transformation with no further configuration.
However, you can add multiple transformers by chaining the `addTransformation()` calls.
The transformations will be applied in the order, they have been submitted to the builder.

By default, every transformation is called once for every chunk of every region of every dimension.
However, you can also configure your `ChunkTransformer` to filter dimensions `targetDimensions()`, regions `targetRegions()` and chunks `targetChunks()`:
```java
public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MCCT.registerTransformer(new ChunkTransformer.Builder()
                .addTransformation(ctx -> {
                    CompoundTag chunkData = ctx.getCompound();
                    // read and write chunkData here...
                    // if you modified the data, remember to call ctx.markDirty() so that your changes get written to disk.
                })
                .targetDimension(World.OVERWORLD::equals)
                .targetRegions(region -> Math.abs(region.x) < 2 && Math.abs(region.y) < 2)
                .targetChunks((chunkPos, region) -> chunkPos.x % 2 == 0 && chunkPos.z % 2 == 0)
                .create());
    }
}
```
This transformer in this example only targets chunks with even coordinates, located in one of the region files `[r.[-1,0,1].[-1,0,1].mca` of the overworld.

### Example
The following example transformer will replace every diamond block in a world with a gold block.
```java
public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MCCT.registerTransformer(new ChunkTransformer.Builder().addTransformation(ctx -> {
            CompoundTag chunkData = ctx.getCompound();
            if (!chunkData.contains("Level", NbtType.COMPOUND)) return;

            CompoundTag level = chunkData.getCompound("Level");
            if (!level.contains("Sections", NbtType.LIST)) return;

            ListTag sections = level.getList("Sections", NbtType.COMPOUND);
            sections.forEach(sectionTag -> {
                if (!(sectionTag instanceof CompoundTag)) return;

                CompoundTag section = (CompoundTag) sectionTag;
                if (!section.contains("Palette", NbtType.LIST)) return;

                ListTag palette = section.getList("Palette", NbtType.COMPOUND);
                palette.forEach(paletteEntryTag -> {
                    if (!(paletteEntryTag instanceof CompoundTag)) return;

                    CompoundTag paletteEntry = (CompoundTag) paletteEntryTag;
                    if (!paletteEntry.contains("Name", NbtType.STRING)) return;

                    String blockId = paletteEntry.getString("Name");
                    if (!"minecraft:diamond_block".equals(blockId)) return;

                    paletteEntry.putString("Name", "minecraft:gold_block");
                    ctx.markDirty();
                });
            });
        }).create());
    }
}
```

## Helpful tools
- [NBTExplorer](https://github.com/jaquadro/NBTExplorer) - a GUI programm that shows you the nbt structure of `.mca` or `.dat` files
