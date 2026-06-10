package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static registry that holds all card data loaded from the client-side JSON resource file.
 * The registry is populated once at class initialization and provides lookup methods
 * to retrieve card attributes by card ID.
 *
 * @see CardData
 */
public class CardRegistry {

    /** Map from card ID to its corresponding {@link CardData}, populated at class initialization. */
    private static final Map<Integer, CardData> registry = new HashMap<>();

    static {
        loadDescriptions();
    }

    /**
     * Loads all card data from the {@code /json/clientCards.json} resource file
     * and populates the registry. Called once during class initialization.
     *
     * @throws RuntimeException if the resource file cannot be found
     */
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

    /**
     * Returns the {@link CardData} associated with the given card ID.
     *
     * @param id the unique identifier of the card
     * @return the corresponding {@link CardData}, or {@code null} if not found
     */
    public static CardData getCard(int id) {
        return registry.get(id);
    }

    /**
     * Returns the display description of the card with the given ID.
     *
     * @param id the unique identifier of the card
     * @return the card's description, or a placeholder message if the card is not found
     */
    public static String getDescription(int id) {
        CardData data = registry.get(id);
        return (data != null) ? data.getDescription() : "Descrizione mancante per ID " + id;
    }

    /**
     * Returns the type of the card with the given ID.
     *
     * @param id the unique identifier of the card
     * @return the {@link CardTypeEnum} of the card
     */
    public static CardTypeEnum getType(int id) {
        CardData data = registry.get(id);
        return data.getType();
    }

    /**
     * Returns the display name of the card with the given ID, derived from its type.
     *
     * @param id the unique identifier of the card
     * @return the display name corresponding to the card's {@link CardTypeEnum}
     */
    public static String getName(int id) {
        CardData data = registry.get(id);
        return switch (data.getType()) {
            case CardTypeEnum.BUILDING      -> "Building ";
            case CardTypeEnum.GATHERER      -> "Gatherer ";
            case CardTypeEnum.HUNTER        -> "Hunter ";
            case CardTypeEnum.PAINTER       -> "Painter ";
            case CardTypeEnum.BUILDER       -> "Builder ";
            case CardTypeEnum.SHAMAN        -> "Shaman ";
            case CardTypeEnum.CRAFTER       -> "Crafter ";
            case CardTypeEnum.FEAST         -> "Feast ";
            case CardTypeEnum.HUNT          -> "Hunt ";
            case CardTypeEnum.STONE_PAINTING-> "Stone_painting ";
            case CardTypeEnum.RITUAL        -> "Ritual ";
        };
    }

    /**
     * Returns the {@link CrafterSymbolEnum} associated with the card with the given ID.
     *
     * @param id the unique identifier of the card
     * @return the crafter symbol of the card, or {@code null} if not a crafter card
     */
    public static CrafterSymbolEnum getSymbol(int id) {
        CardData data = registry.get(id);
        return data.getSymbol();
    }

    /**
     * Returns the food cost of the card with the given ID.
     *
     * @param id the unique identifier of the card
     * @return the food cost of the card, or {@code 0} if not applicable
     */
    public static int getCost(int id) {
        CardData data = registry.get(id);
        return data.getCost();
    }

    /**
     * Returns the set of all card IDs currently loaded in the registry.
     *
     * @return a {@link Set} containing all registered card IDs
     */
    public static Set<Integer> getIds() {
        return registry.keySet();
    }
}