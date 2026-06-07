package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.CardData;
import it.polimi.ingsw.client.data.CardRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CardImagesLoader {
    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();
    private final Map<String, Image> stringToBack = new ConcurrentHashMap<>();

    private static CardImagesLoader instance;

    public static synchronized CardImagesLoader getInstance(){
        if(instance == null){
            instance = new CardImagesLoader();
        }
        return instance;
    }

    private String generateBackKey(CardData data){
        return data.getBackImagePath();
    }

    private Image loadImage(String path){
        try{
            var resource = getClass().getResource(path);
            if(resource == null){
                throw new Exception("[CardImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(),false);
        } catch (Exception e){
            System.err.println("[CardImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    private void loadFront(int id){
        String path = String.format("/images/cards/Card_%03d.png", id);

        Image img = loadImage(path);
        if(img != null){
            idToFront.put(id, img);
        }
    }

    private void loadBack(String key){
        String path = String.format("/images/cards/%s.png", key);
        Image img = loadImage(path);
        if(img != null){ stringToBack.put(key, img); }
    }

    public void loadAll() {

        for(int id : CardRegistry.getIds()){
            CardData data = CardRegistry.getCard(id);

            loadFront(id);

            String key = generateBackKey(data);

            if(!stringToBack.containsKey(key)){
                loadBack(key);
            }
        }
        System.out.println("Tutte le carte sono state caricate.");
    }

    public Image getFront(int id){
        return idToFront.get(id);
    }

    public Image getBack(int id){
        CardData data = CardRegistry.getCard(id);
        if(data == null){ return null; }
        return stringToBack.get(generateBackKey(data));
    }
}
