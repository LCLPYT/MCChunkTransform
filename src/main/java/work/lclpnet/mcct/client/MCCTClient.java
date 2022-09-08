package work.lclpnet.mcct.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.ChunkPos;
import work.lclpnet.mcct.transform.ChunkTransformer;
import work.lclpnet.mcct.transform.MCCT;
import work.lclpnet.mcct.transform.impl.RegexFindReplaceChunkTransformer;

public class MCCTClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        final var testTransformer = new RegexFindReplaceChunkTransformer(
                "minecraft:stone_(slab|stairs)(\\[[a-z_]+=[a-z_]+\\])?",
                "minecraft:oak_$1$2"
        );

        MCCT.registerTransformer(new ChunkTransformer.Builder()
                .targetChunks((chunkPos, regionFileLocation) -> ChunkPos.ORIGIN.equals(chunkPos))
                .addTransformation(testTransformer)
                .create());
    }
}
