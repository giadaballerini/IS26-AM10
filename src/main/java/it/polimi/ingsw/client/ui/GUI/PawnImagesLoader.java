package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PawnImagesLoader {
    private static PawnImagesLoader instance;

    private final Map<ColorPawnEnum, Image> colorToTotem = new ConcurrentHashMap<>();

    public static synchronized PawnImagesLoader getInstance() {
        if (instance == null) {
            instance = new PawnImagesLoader();
        }
        return instance;
    }

    public void loadPawns(){
        for(ColorPawnEnum color : ColorPawnEnum.values()){

            Image img = loadPawn(color);
            if(img != null){
                colorToTotem.put(color, img);
            }
        }
        System.out.println("Tutti i pawn  sono stati creati!");
    }

    private Image loadPawn(ColorPawnEnum color){

        String name = switch(color) {
            case BLUE -> "blue";
            case YELLOW -> "yellow";
            case WHITE -> "white";
            case ORANGE -> "orange";
            default -> "purple";
        };
        String path = String.format("/images/pawns/pawn_%s.png", name);

        return loadImage(path);

    }

    private Image loadImage(String path){
        try{
            var resource = getClass().getResource(path);
            if(resource == null){
                throw new Exception("[PawnImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(), false);
        } catch (Exception e){
            System.err.println("[PawnImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    public Image getTotemImage(ColorPawnEnum color){
        return colorToTotem.get(color);
    }
}
