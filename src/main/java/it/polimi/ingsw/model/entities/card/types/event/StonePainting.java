package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class StonePainting extends Event {
    private final int nPainterSup;
    private final int ppGain;
    private final int ppLoss;

    @JsonCreator
    public StonePainting(@JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                         @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                         @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                         @JsonProperty("age") int age,@JsonProperty("nPainterSup") int nPainterSup,
                         @JsonProperty("ppGain") int ppGain,@JsonProperty("ppLoss") int ppLoss,
                         @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.nPainterSup = nPainterSup;
        this.ppGain = ppGain;
        this.ppLoss = ppLoss;
    }

    public void execEvent(List<Player> players, GamePhaseEnum phase){
        if(phase == GamePhaseEnum.END_ROUND){
            for(Player playerRef : players){
                int nPaintersActual = playerRef.getNumType(CardTypeEnum.PAINTER);
                if(nPaintersActual <= this.nPainterSup){
                    playerRef.addPP(-this.ppLoss);
                } else {
                    playerRef.addPP(this.ppGain * nPaintersActual);
                }
            }
        }
    }
}
