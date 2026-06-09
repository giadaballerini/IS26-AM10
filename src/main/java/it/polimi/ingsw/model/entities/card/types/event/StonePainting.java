package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * An event card that rewards players with many painters and penalises those
 * with few.
 *
 * <p>When resolved during {@link GamePhaseEnum#END_ROUND}, the paint bonus is
 * applied to each player first. Then, players whose painter count exceeds
 * {@link #nPainterSup} gain {@link #ppGain} prestige points per painter;
 * all other players lose {@link #ppLoss} prestige points.</p>
 */
public class StonePainting extends Event {

    /**
     * The painter count threshold: players with this many painters or fewer
     * are penalised; those with more are rewarded.
     */
    private final int nPainterSup;

    /** Prestige points gained per painter for players above the threshold. */
    private final int ppGain;

    /** Prestige points lost by players at or below the painter threshold. */
    private final int ppLoss;

    /**
     * Constructs a {@code StonePainting} event from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase during which this event is resolved
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param nPainterSup        painter count threshold
     * @param ppGain             PP gained per painter for players above the threshold
     * @param ppLoss             PP lost by players at or below the threshold
     * @param type               the card type (always {@link CardTypeEnum#STONE_PAINTING})
     */
    @JsonCreator
    public StonePainting(@JsonProperty("id") int id,
                         @JsonProperty("trigger") GamePhaseEnum trigger,
                         @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                         @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                         @JsonProperty("age") int age,
                         @JsonProperty("nPainterSup") int nPainterSup,
                         @JsonProperty("ppGain") int ppGain,
                         @JsonProperty("ppLoss") int ppLoss,
                         @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.nPainterSup = nPainterSup;
        this.ppGain = ppGain;
        this.ppLoss = ppLoss;
    }

    /**
     * Resolves the stone painting event for all players during
     * {@link GamePhaseEnum#END_ROUND}.
     *
     * <p>The paint bonus is applied to each player first. Players with more
     * than {@link #nPainterSup} painters gain {@code ppGain × painterCount}
     * prestige points; all others lose {@link #ppLoss} prestige points.</p>
     *
     * @param players the list of all players; must not be {@code null}
     * @param phase   the current game phase
     */
    @Override
    public void execEvent(List<Player> players, GamePhaseEnum phase) {
        if (phase == GamePhaseEnum.END_ROUND) {
            for (Player playerRef : players) {
                playerRef.applyPaintBonus();
                int nPaintersActual = playerRef.getNumType(CardTypeEnum.PAINTER);
                if (nPaintersActual <= this.nPainterSup) {
                    playerRef.addPP(-this.ppLoss);
                } else {
                    playerRef.addPP(this.ppGain * nPaintersActual);
                }
            }
        }
    }
}