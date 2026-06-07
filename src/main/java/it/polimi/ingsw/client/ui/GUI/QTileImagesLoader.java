package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.QTileRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QTileImagesLoader {
    private static QTileImagesLoader instance;

    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();

    public static synchronized QTileImagesLoader getInstance() {
        if (instance == null) {
            instance = new QTileImagesLoader();
        }
        return instance;
    }

    public void loadQTiles(){
        for(Integer id : QTileRegistry.getIds()){

            loadFront(id);
        }
        System.out.println("Tutte le Tile sono state create!");
    }

    private void loadFront(int id){
        String path = String.format("/images/qtiles/QTiles_%d.jpg", id);

        Image img = loadImage(path);
        if(img != null){
            idToFront.put(id, img);
        }
    }

    private Image loadImage(String path){
        try{
            var resource = getClass().getResource(path);
            if(resource == null){
                throw new Exception("[QTileImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(), false);
        } catch (Exception e){
            System.err.println("[QTileImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    public Image getFront(int id){
        return idToFront.get(id);
    }
}
