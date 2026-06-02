package it.polimi.ingsw.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

public class QTileRegistry {
    private static final Map<Integer, QTileData> reg = new HashMap<>();

    static {
        loadTiles();
    }

    private static void loadTiles() {
        ObjectMapper mapper = new ObjectMapper();

        try(InputStream is = QTileRegistry.class.getResourceAsStream("/json/clientQTiles.json")) {

            if(is == null) {
                throw new RuntimeException("Risorsa clientQtiles.json non trovata");
            }
            List<QTileData> tiles = mapper.readValue(is, new  TypeReference<List<QTileData>>() {});

            for(QTileData tile : tiles) {
                reg.put(tile.getId(), tile);
            }
            System.out.println("[JACKSON] QTiles caricate con successo!");

        }catch(Exception e) {
            System.out.println("[ERRORE CRITICO] Fallimento nel caricamento qtiles.json");
            e.printStackTrace();
        }
    }

    public static QTileData getTile(int id) {
        return reg.get(id);
    }

    public static Set<Integer> getIds(){
        return reg.keySet();
    }

    public static String getName(){
        return "Queue Tile ";
    }
    public static String getDescription(int id){
        return getTile(id).getDescription();
    }
}
