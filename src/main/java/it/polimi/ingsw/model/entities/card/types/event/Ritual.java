package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class Ritual extends Event {

    private final int ppLoss;
    private final int ppGain;

    @JsonCreator
    public Ritual(@JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                  @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                  @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                  @JsonProperty("age") int age,@JsonProperty("ppLoss") int ppLoss,@JsonProperty("ppGain") int ppGain,
                  @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppLoss = ppLoss;
        this.ppGain = ppGain;
    }

    public void execEvent(List<Player> players, GamePhaseEnum phase){
        if(phase == GamePhaseEnum.END_ROUND || phase == GamePhaseEnum.PLAY_EVENT) {
            int maxStars;
            int minStars;
            maxStars = players.stream().map(Player::getNStars).max(Integer::compareTo).get();
            minStars = players.stream().map(Player::getNStars).min(Integer::compareTo).get();
            for (Player player : players) {
                if (player.getNStars() == maxStars) {
                    if(player.getHasDoubleShamanIncome())
                        player.addPP(ppGain * 2);
                    else
                        player.addPP(ppGain);
                }
                if (player.getNStars() == minStars) {
                    if(!player.getHasProtection()) {
                        if (player.getHasDoubleShamanIncome())
                            player.addPP(-ppLoss * 2);
                        else
                            player.addPP(-ppLoss);
                    }
                }

            }
        }

    }
}
