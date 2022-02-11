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

## Create custom transformations
In order to apply custom transformation to your world, you need to implement a `IChunkTransformer` in your mod.
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
        MCCT.registerTransformer(ctx -> {
            CompoundTag chunkData = ctx.getCompound();
            // read and write chunkData here...
            // if you modified the data, remember to call ctx.markDirty() so that your changes get written to disk.
        });
    }
}
```
The transformer will be called for every chunk in your world, including other dimensions.

### Example
The following example transformer will replace every diamond block in a world with a gold block.
```java
public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        MCCT.registerTransformer(ctx -> {
            CompoundTag chunkData = ctx.getCompound();
            
            // check if the root compound tag has a "Level" entry of type compound
            if (!chunkData.contains("Level", NbtType.COMPOUND)) return;
            CompoundTag level = chunkData.getCompound("Level");
            
            // check if the compound tag has a "Sections" entry of type list
            if (!level.contains("Sections", NbtType.LIST)) return;
            ListTag sections = level.getList("Sections", NbtType.COMPOUND);  // get a ListTag that stores CompoundTags
            
            // iterate sections
            sections.forEach(sectionTag -> {
                if (!(sectionTag instanceof CompoundTag)) return;
                CompoundTag section = (CompoundTag) sectionTag;
                
                // check if the compound tag has a "Palette" entry of type list
                if (!section.contains("Palette", NbtType.LIST)) return;
                ListTag palette = section.getList("Palette", NbtType.COMPOUND);
                
                // iterate block palette entries
                palette.forEach(paletteEntryTag -> {
                    if (!(paletteEntryTag instanceof CompoundTag)) return;
                    CompoundTag paletteEntry = (CompoundTag) paletteEntryTag;
                    
                    // check if the compound tag has a "Name" entry of type string
                    if (!paletteEntry.contains("Name", NbtType.STRING)) return;

                    // get the block identifier (e.g. "minecraft:dirt")
                    String blockId = paletteEntry.getString("Name");
                    if (!"minecraft:diamond_block".equals(blockId)) return;
                    
                    // the palette entry is a diamond block, replace this with gold block:
                    paletteEntry.putString("Name", "minecraft:gold_block");
                    
                    // mark dirty to indicate that this chunk should be written to disk
                    ctx.markDirty();
                });
            });
        });
    }
}
```

## Helpful tools
- [NBTExplorer](https://github.com/jaquadro/NBTExplorer) - a GUI programm that shows you the nbt structure of `.mca` or `.dat` files
