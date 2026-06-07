package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.TileData;
import it.polimi.ingsw.client.data.TileRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TileImagesLoader {
    private static TileImagesLoader instance;

    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();

    public static synchronized TileImagesLoader getInstance() {
        if (instance == null) {
            instance = new TileImagesLoader();
        }
        return instance;
    }

    public void loadTiles(){
        for(Integer id : TileRegistry.getIds()){

            TileData data = TileRegistry.getTile(id);

            loadFront(id);
        }
        System.out.println("Tutte le Tile sono state create!");
    }

    private void loadFront(int id){
        String path = String.format("/images/tiles/Tile_%d.png", id);

        Image img = loadImage(path);
        if(img != null){
            idToFront.put(id, img);
        }
    }

    private Image loadImage(String path){
        try{
            var resource = getClass().getResource(path);
            if(resource == null){
                throw new Exception("[TileImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(),false);
        } catch (Exception e){
            System.err.println("[TileImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    public Image getFront(int id){
        return idToFront.get(id);
    }
}
