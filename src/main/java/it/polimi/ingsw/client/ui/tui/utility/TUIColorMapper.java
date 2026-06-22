package it.polimi.ingsw.client.ui.tui.utility;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import org.jline.utils.AttributedStyle;

/**
 * Utility class that maps player pawn colors to their corresponding JLine ANSI styles
 * for use in the TUI. Orange and purple are approximated using 256-color terminal codes,
 * as they have no direct equivalent in the standard 8-color ANSI palette.
 *
 * @see CardColorMapper
 */
public class TUIColorMapper {

    /** 256-color terminal code approximating orange, used for {@link ColorPawnEnum#ORANGE}. */
    private static final int orangeCode = 166;

    /** 256-color terminal code approximating purple, used as the default pawn color. */
    private static final int purpleCode = 129;

    /**
     * Returns the JLine {@link AttributedStyle} foreground color associated with the
     * given player pawn color.
     *
     * @param color the pawn color of the player
     * @return the {@link AttributedStyle} with the appropriate foreground color;
     *         defaults to purple for unrecognized or {@link ColorPawnEnum#PURPLE} values
     */
    public static AttributedStyle getPlayerJlineColor(ColorPawnEnum color) {
        AttributedStyle style = AttributedStyle.DEFAULT;
        return switch (color) {
            case BLUE -> style.foreground(AttributedStyle.BLUE);
            case YELLOW -> style.foreground(AttributedStyle.YELLOW);
            case WHITE -> style.foreground(AttributedStyle.WHITE);
            case ORANGE -> style.foreground(orangeCode);
            default -> style.foreground(purpleCode);
        };
    }
    /**
     * Private constructor to prevent instantiation of this static utility class.
     */
    private TUIColorMapper() {
    }
}