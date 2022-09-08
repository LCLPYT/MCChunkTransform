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
     * This transformer will find any strings and replaces <code>target</code> with <code>replacement</code>.
     *
     * @param target The string to search; will be replaced with <code>replacement</code>.
     * @param replacement The string that will be inserted.
     */
    public StringFindReplaceChunkTransformer(String target, String replacement) {
        this.searchString = Objects.requireNonNull(target);
        this.replaceValue = Objects.requireNonNull(replacement);
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
        if (!shouldReplace(string)) return;

        String val = replace(string);
        parent.putString(key, val);
        ctx.markDirty();
    }

    protected boolean shouldReplace(String s) {
        return s != null && s.contains(searchString);
    }

    protected String replace(String s) {
        return s.replace(searchString, replaceValue);
    }
}
