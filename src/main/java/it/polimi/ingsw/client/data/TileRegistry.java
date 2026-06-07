package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TileRegistry {

    private static final Map<Integer, TileData> reg = new LinkedHashMap<>();

    static {
        loadTiles();
    }

    private static void loadTiles() {
        ObjectMapper mapper = new ObjectMapper();

        try(InputStream is = TileRegistry.class.getResourceAsStream("/json/clientTiles.json")) {

            if(is == null) {
                throw new RuntimeException("Risorsa clientTiles.json non trovata");
            }
            List<TileData> tiles = mapper.readValue(is, new  TypeReference<List<TileData>>() {});

            for(TileData tile : tiles) {
                reg.put(tile.getId(), tile);
            }
            System.out.println("[JACKSON] Tiles caricate con successo!");

        }catch(Exception e) {
            System.out.println("[ERRORE CRITICO] Fallimento nel caricamento tiles.json");
            e.printStackTrace();
        }
    }

    public static TileData getTile(int id) {
        return reg.get(id);
    }

    public static Set<Integer> getIds(){
        return reg.keySet();
    }

    public static String getName(){
        return "Tile ";
    }
    public static String getDescription(int id){
        return getTile(id).getDescription();
    }
}
