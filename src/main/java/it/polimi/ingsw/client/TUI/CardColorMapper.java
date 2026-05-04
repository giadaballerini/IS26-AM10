package it.polimi.ingsw.client.TUI;

import org.jline.utils.AttributedStyle;

public class CardColorMapper {
    private static int rgbToInt(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }
    private static final int colorHunter = rgbToInt(238, 78, 60);
    private static final int colorBuilder = rgbToInt(72, 8, 31);
    private static final int colorGatherer = rgbToInt(243, 130, 53);
    private static final int colorPainter = rgbToInt(245, 203, 43);
    private static final int colorShaman = rgbToInt(10, 122, 184);
    private static final int colorCrafter = rgbToInt(139, 65, 93);

    public static AttributedStyle getCardJlineColor(String cardType){
        AttributedStyle style = AttributedStyle.DEFAULT;
        switch(cardType.toLowerCase()){
            case "gatherer":
                return style.foreground(243, 130, 53);
            case "hunter":
                return style.foreground(238, 78, 60);
            case "builder":
                return style.foreground(72, 8, 31);
            case "painter":
                return style.foreground(245, 203, 43);
            case "shaman":
                return style.foreground(139, 65, 93);
            case "crafter":
                return style.foreground(65, 183, 171);
            default:
                return AttributedStyle.DEFAULT;
        }
    }
}
