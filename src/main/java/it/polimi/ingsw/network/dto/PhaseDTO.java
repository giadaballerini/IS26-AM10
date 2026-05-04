package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

import java.io.Serial;
import java.io.Serializable;

public class PhaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private GamePhaseEnum gamePhase;

    public PhaseDTO(GamePhaseEnum gamePhase) {
        this.gamePhase = gamePhase;
    }

    public GamePhaseEnum getPhase() {
        return gamePhase;
    }

}
