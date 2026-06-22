package it.polimi.ingsw.client.ui.tui.utility;

import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

import java.util.Map;

/**
 * Utility class that maps {@link CrafterSymbolEnum} values to their corresponding
 * Unicode emoji representations for display in the TUI. Each crafter card carries
 * a symbol that identifies the type of craft item it represents.
 */
public class CrafterSymbolMapper {

    /**
     * Map from crafter symbol name to its Unicode emoji representation.
     * Keys match the names of {@link CrafterSymbolEnum} constants in uppercase.
     */
    private static final Map<String, String> SYMBOLS = Map.of(
            "AMIGDALA",  "\uD83D\uDCA0",  // 🔠
            "ARROWHEAD", "\uD83C\uDFF9",  // 🏹
            "LEATHER",   "\uD83D\uDCDC",  // 📜
            "BREAD",     "\uD83E\uDD56",  // 🥖
            "FLUTE",     "\uD83C\uDFB6",  // 🎶
            "BOWL",      "\uD83E\uDD63",  // 🥣
            "ROPE",      "\uD83E\uDEA2",  // 🪢
            "DOLL",      "\uD83E\uDDF8",  // 🧸
            "NECKLACE",  "\uD83D\uDCFF",  // 📿
            "HOOK",      "\uD83C\uDFA3"   // 🎣
    );

    /**
     * Returns the Unicode emoji corresponding to the given symbol name.
     *
     * @param symbolName the crafter symbol name (case-insensitive);
     *                   if {@code null} or unrecognized, returns {@code "?"}
     * @return the emoji string for the symbol, or {@code "?"} if not found
     */
    public static String getSymbol(String symbolName) {
        return SYMBOLS.getOrDefault(
                symbolName != null ? symbolName.toUpperCase() : "",
                "?"
        );
    }

    /**
     * Returns the Unicode emoji corresponding to the given {@link CrafterSymbolEnum} value.
     *
     * @param symbol the crafter symbol enum value;
     *               if {@code null}, returns {@code "?"}
     * @return the emoji string for the symbol, or {@code "?"} if {@code null}
     */
    public static String getSymbol(CrafterSymbolEnum symbol) {
        return symbol != null ? getSymbol(symbol.name()) : "?";
    }
    /** Prevents instantiation of this utility class; all members are static. */
    private CrafterSymbolMapper() { }
}