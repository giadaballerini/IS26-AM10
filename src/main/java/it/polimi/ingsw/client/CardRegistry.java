package it.polimi.ingsw.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CardRegistry {
    private static final Map<Integer, CardData> registry = new HashMap<>();

    static {
        loadDescriptions();
    }
    private static void loadDescriptions() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = CardRegistry.class.getResourceAsStream("/json/clientCards.json")) {
            if (is == null) {
                throw new RuntimeException("Impossibile trovare clientCards.json nelle risorse.");
            }
            List<CardData> cards = mapper.readValue(is, new TypeReference<List<CardData>>() {});

            for (CardData card : cards) {
                registry.put(card.getId(), card);
            }

        } catch (Exception e) {
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
        return data.getType();
    }

    public static String getName(int id){
        CardData data = registry.get(id);
        return switch(data.getType()){
            case CardTypeEnum.BUILDING -> "Building ";
            case CardTypeEnum.GATHERER -> "Gatherer ";
            case CardTypeEnum.HUNTER -> "Hunter ";
            case CardTypeEnum.PAINTER -> "Painter ";
            case CardTypeEnum.BUILDER -> "Builder ";
            case CardTypeEnum.SHAMAN -> "Shaman ";
            case CardTypeEnum.CRAFTER -> "Crafter ";
            case CardTypeEnum.FEAST -> "Feast ";
            case CardTypeEnum.HUNT -> "Hunt ";
            case CardTypeEnum.STONE_PAINTING -> "Stone_painting ";
            case CardTypeEnum.RITUAL -> "Ritual ";
        };
    }


    public static CrafterSymbolEnum getSymbol(int id) {
        CardData data = registry.get(id);
        return data.getSymbol();
    }

    public static int getCost(int id) {
        CardData data = registry.get(id);
        return data.getCost();
    }


    public static Set<Integer> getIds() {
        return registry.keySet();
    }

}
