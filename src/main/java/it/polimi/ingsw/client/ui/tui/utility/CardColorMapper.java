package it.polimi.ingsw.client.ui.tui.utility;

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
        return switch (cardType.toLowerCase()) {
            case "gatherer" -> style.foreground(243, 130, 53);
            case "hunter" -> style.foreground(238, 78, 60);
            case "builder" -> style.foreground(129, 69, 89);
            case "painter" -> style.foreground(245, 203, 43);
            case "shaman" -> style.foreground(139, 65, 93);
            case "crafter" -> style.foreground(65, 183, 171);
            default -> AttributedStyle.DEFAULT;
        };
    }
    /** Prevents instantiation of this utility class; all members are static. */
    private CardColorMapper() { }
}