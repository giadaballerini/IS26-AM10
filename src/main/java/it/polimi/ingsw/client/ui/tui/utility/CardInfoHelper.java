package it.polimi.ingsw.client.ui.tui.utility;

import it.polimi.ingsw.client.data.CardData;
import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.network.dto.CardDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that formats card details for display in the TUI.
 */
public class CardInfoHelper {

    /**
     * Returns a list of formatted strings describing the given card, suitable for
     * display in the TUI. All cards include their ID, age, type, and description.
     * {@link CardTypeEnum#BUILDING} cards additionally include their food cost
     * and Prestige Points value.
     *
     * @param card the card DTO received from the server
     * @return a list of formatted detail lines describing the card
     */
    public static List<String> getFormattedDetails(CardDTO card) {
        List<String> details = new ArrayList<>();
        CardTypeEnum type = card.getType();
        CardData data = CardRegistry.getCard(card.getId());
        details.add(String.format("ID: %d | Era: %d | Tipo: %s", card.getId(), data.getAge(), type));
        if (type == CardTypeEnum.BUILDING) {
            details.add("Costo: " + data.getCost());
            details.add("Punti Prestigio: " + data.getPp());
        }
        details.add("Descrizione: " + data.getDescription());
        return details;
    }
    /** Prevents instantiation of this utility class; all members are static. */
    private CardInfoHelper() { }
}