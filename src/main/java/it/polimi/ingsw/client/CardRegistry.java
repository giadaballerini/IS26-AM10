package it.polimi.ingsw.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardRegistry {
    private static final Map<Integer, CardData> registry = new HashMap<>();

    static {
        loadDescriptions();
    }
    private static void loadDescriptions() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = CardRegistry.class.getClassLoader().getResourceAsStream("clientCards.json")) {
            if (is == null) {
                throw new RuntimeException("Impossibile trovare clientCards.json nelle risorse.");
            }
            List<CardData> cards = mapper.readValue(is, new TypeReference<List<CardData>>() {});

            for (CardData card : cards) {
                registry.put(card.getId(), card);
            }
            System.out.println("[JACKSON] Caricate " + registry.size() + " carte con successo.");

        } catch (Exception e) {
            // Gestione robusta dell'errore per l'esame
            System.err.println("[ERRORE CRITICO] Fallimento nel parsing delle carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CardData getCard(int id) {
        return registry.get(id);
    }

    public static String getDescription(int id) {
        CardData data = registry.get(id);
        return (data != null) ? data.getDescription() : "Descrizione mancante per ID " + id;
    }

    public static CardTypeEnum getType(int id) {
        CardData data = registry.get(id);
        return data.getName();
    }

    public static CrafterSymbolEnum getSymbol(int id) {
        CardData data = registry.get(id);
        return data.getSymbol();
    }

    public static int getCost(int id) {
        CardData data = registry.get(id);
        return data.getCost();
    }


}
