package work.lclpnet.mcct.transform.impl;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import work.lclpnet.mcct.transform.ChunkTransformContext;
import work.lclpnet.mcct.transform.ChunkTransformer;
import work.lclpnet.mcct.transform.IChunkTransformation;

import java.util.Objects;

/**
 * A chunk transformer that finds and replaces strings recursively in the chunk data.
 * This works just like your text editor's find and replace.
 */
public class StringFindReplaceChunkTransformer implements IChunkTransformation {

    protected final String searchString;
    protected final String replaceValue;

    /**
     * Create a new find and replace transformer.
     * This transformer will find any strings and replaces <code>searchString</code> with <code>replaceValue</code>.
     *
     * @param searchString The string to search; will be replaced with <code>replaceValue</code>.
     * @param replaceValue The string that will be inserted.
     */
    public StringFindReplaceChunkTransformer(String searchString, String replaceValue) {
        this.searchString = Objects.requireNonNull(searchString);
        this.replaceValue = Objects.requireNonNull(replaceValue);
    }

    @Override
    public void transform(ChunkTransformContext ctx, ChunkTransformer transformer) {
        final NbtCompound compound = ctx.getCompound();
        visitCompound(compound, ctx);
    }

    protected void visitCompound(NbtCompound compound, ChunkTransformContext ctx) {
        if (compound == null) return;

        compound.getKeys().forEach(key -> {
            NbtElement tag = compound.get(key);
            if (tag instanceof NbtCompound) visitCompound((NbtCompound) tag, ctx);
            else if (tag instanceof NbtList) visitList((NbtList) tag, ctx);
            else if (tag instanceof NbtString) visitString(tag.asString(), compound, key, ctx);
        });
    }

    protected void visitList(NbtList list, ChunkTransformContext ctx) {
        if (list == null || list.getHeldType() != NbtType.COMPOUND) return;
        list.forEach(tag -> visitCompound((NbtCompound) tag, ctx));
    }


    protected void visitString(String string, NbtCompound parent, String key, ChunkTransformContext ctx) {
        if (!string.contains(searchString)) return;

        String val = string.replace(searchString, replaceValue);
        parent.putString(key, val);
        ctx.markDirty();
    }
}
