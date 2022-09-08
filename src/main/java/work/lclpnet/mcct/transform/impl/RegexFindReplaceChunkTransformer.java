package work.lclpnet.mcct.transform.impl;

public class RegexFindReplaceChunkTransformer extends StringFindReplaceChunkTransformer {

    /**
     * Create a new find and replace transformer supporting regex.
     * This transformer will find any strings matching <code>pattern</code>
     * and replaces all matches with <code>replacement</code>.
     *
     * Supports substituting groups with <code>$n</code> identifiers, where <code>n</code> is the match group.
     * Uses the {@link String#replaceAll(String, String)} method.
     *
     * <br><br>
     * Example:
     * <pre>
     * {@code
     * new RegexFindReplaceChunkTransformer("minecraft:stone_(slab|stairs)", "minecraft:oak_$1");
     * }
     * </pre>
     * This will replace every stone_slab block with oak_slab and every stone_stairs with oak_stairs.
     *
     * @param pattern The string to search; will be replaced with <code>replaceValue</code>.
     * @param replaceValue The string that will be inserted.
     */
    public RegexFindReplaceChunkTransformer(String pattern, String replaceValue) {
        super(pattern, replaceValue);
    }

    @Override
    protected boolean shouldReplace(String s) {
        return s.matches(searchString);
    }

    @Override
    protected String replace(String s) {
        return s.replaceAll(searchString, replaceValue);
    }
}
