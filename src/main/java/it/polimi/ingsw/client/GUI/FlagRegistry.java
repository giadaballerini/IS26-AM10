package it.polimi.ingsw.client.GUI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlagRegistry {
    private static final Map<String, String> reg = new HashMap<>();

    static {
        loadFlags();
    }

    private static void loadFlags() {
        ObjectMapper mapper = new ObjectMapper();

        try(InputStream is = FlagRegistry.class.getResourceAsStream("/json/clientFlags.json")) {

            if(is == null) {
                throw new RuntimeException("Risorsa clientFlags.json non trovata");
            }
            List<FlagData> descriptions = mapper.readValue(is, new  TypeReference<List<FlagData>>() {});

            for(FlagData flagData : descriptions) {
                reg.put(flagData.getName(), flagData.getDescription());
            }
            System.out.println("[JACKSON] Flags caricate con successo!");

        }catch(Exception e) {
            System.out.println("[ERRORE CRITICO] Fallimento nel caricamento clientFlags.json");
            e.printStackTrace();
        }
    }

    public static String getDescription(String flag){
        return reg.get(flag);
    }
}
