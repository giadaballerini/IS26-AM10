package it.polimi.ingsw.client.ui.TUI.utility;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import org.jline.utils.AttributedStyle;

public class TUIColorMapper {
    private final static int orangeCode = 214;
    private final static int purpleCode = 129;
    public static AttributedStyle getPlayerJlineColor(ColorPawnEnum color) {
        AttributedStyle style = AttributedStyle.DEFAULT;
        switch (color) {
            case BLUE: return style.foreground(AttributedStyle.BLUE);
            case YELLOW: return style.foreground(AttributedStyle.YELLOW);
            case WHITE: return style.foreground(AttributedStyle.WHITE);
            case ORANGE: return style.foreground(orangeCode);
            default: return style.foreground(purpleCode);
        }
    }

}
