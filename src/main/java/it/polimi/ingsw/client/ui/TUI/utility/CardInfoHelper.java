package it.polimi.ingsw.client.ui.TUI.utility;

import it.polimi.ingsw.client.data.CardData;
import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.network.dto.CardDTO;

import java.util.ArrayList;
import java.util.List;

public class CardInfoHelper {

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
}
