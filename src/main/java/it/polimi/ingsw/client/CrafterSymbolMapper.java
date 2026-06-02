package it.polimi.ingsw.client;

import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

import java.util.Map;

public class CrafterSymbolMapper {
    private static final Map<String, String> SYMBOLS = Map.of(
            "AMIGDALA",  "\uD83D\uDCA0",
            "ARROWHEAD", "\uD83C\uDFF9",
            "LEATHER",   "\uD83D\uDCDC",
            "BREAD",     "\uD83E\uDD56",
            "FLUTE",     "\uD83C\uDFB6",
            "BOWL",      "\uD83E\uDD63",
            "ROPE",      "\uD83E\uDEA2",
            "DOLL",      "\uD83E\uDDF8",
            "NECKLACE",  "\uD83D\uDCFF",
            "HOOK",      "\uD83C\uDFA3"
    );

    public static String getSymbol(String symbolName) {
        return SYMBOLS.getOrDefault(
                symbolName != null ? symbolName.toUpperCase() : "",
                "?"
        );
    }

    public static String getSymbol(CrafterSymbolEnum symbol) {
        return symbol != null ? getSymbol(symbol.name()) : "?";
    }
}
