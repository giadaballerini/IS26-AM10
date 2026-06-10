package it.polimi.ingsw.client.ui.TUI.utility;

import org.jline.utils.AttributedStyle;

/**
 * Utility class that maps character card types to their corresponding ANSI RGB colors
 * for use in the TUI. Each card type has a distinct color that matches its visual
 * identity in the GUI, applied to text rendered via JLine's {@link AttributedStyle}.
 *
 * @see TUIColorMapper
 */
public class CardColorMapper {

    /**
     * Converts separate red, green, and blue components into a single packed RGB integer.
     *
     * @param r the red component (0–255)
     * @param g the green component (0–255)
     * @param b the blue component (0–255)
     * @return the packed RGB integer
     */
    private static int rgbToInt(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    /** Packed RGB color for Hunter cards. */
    private static final int colorHunter   = rgbToInt(238, 78,  60);

    /** Packed RGB color for Builder cards. */
    private static final int colorBuilder  = rgbToInt(72,  8,   31);

    /** Packed RGB color for Gatherer cards. */
    private static final int colorGatherer = rgbToInt(243, 130, 53);

    /** Packed RGB color for Painter cards. */
    private static final int colorPainter  = rgbToInt(245, 203, 43);

    /** Packed RGB color for Shaman cards. */
    private static final int colorShaman   = rgbToInt(10,  122, 184);

    /** Packed RGB color for Crafter cards. */
    private static final int colorCrafter  = rgbToInt(139, 65,  93);

    /**
     * Returns the JLine {@link AttributedStyle} foreground color associated with the
     * given card type name. The color matches the card type's visual identity used
     * in the GUI. Unrecognized card types return the default style.
     *
     * @param cardType the card type name (case-insensitive), corresponding to a
     *                 character {@link it.polimi.ingsw.enumerations.CardTypeEnum} value
     * @return the {@link AttributedStyle} with the appropriate foreground color,
     *         or {@link AttributedStyle#DEFAULT} if the card type is not recognized
     */
    public static AttributedStyle getCardJlineColor(String cardType) {
        AttributedStyle style = AttributedStyle.DEFAULT;
        switch (cardType.toLowerCase()) {
            case "gatherer": return style.foreground(243, 130, 53);
            case "hunter":   return style.foreground(238, 78,  60);
            case "builder":  return style.foreground(129, 69,  89);
            case "painter":  return style.foreground(245, 203, 43);
            case "shaman":   return style.foreground(139, 65,  93);
            case "crafter":  return style.foreground(65,  183, 171);
            default:         return AttributedStyle.DEFAULT;
        }
    }
}