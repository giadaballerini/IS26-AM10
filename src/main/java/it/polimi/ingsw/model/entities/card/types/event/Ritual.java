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
 * An event card that rewards the player with the most stars and penalises
 * the player with the fewest.
 *
 * <p>When resolved during {@link GamePhaseEnum#END_ROUND} or
 * {@link GamePhaseEnum#PLAY_EVENT}, the player(s) with the highest star count
 * gain {@link #ppGain} prestige points (doubled if the double shaman bonus is
 * active), and the player(s) with the lowest star count lose {@link #ppLoss}
 * prestige points (unless protected by PP loss protection).</p>
 */
public class Ritual extends Event {

    /** Prestige points lost by the player(s) with the fewest stars. */
    private final int ppLoss;

    /** Base prestige points gained by the player(s) with the most stars. */
    private final int ppGain;

    /**
     * Constructs a {@code Ritual} event from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase during which this event is resolved
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param ppLoss             PP deducted from the player(s) with the fewest stars
     * @param ppGain             base PP awarded to the player(s) with the most stars
     * @param type               the card type (always {@link CardTypeEnum#RITUAL})
     */
    @JsonCreator
    public Ritual(@JsonProperty("id") int id,
                  @JsonProperty("trigger") GamePhaseEnum trigger,
                  @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                  @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                  @JsonProperty("age") int age,
                  @JsonProperty("ppLoss") int ppLoss,
                  @JsonProperty("ppGain") int ppGain,
                  @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppLoss = ppLoss;
        this.ppGain = ppGain;
    }

    /**
     * Resolves the ritual for all players.
     *
     * <p>The player(s) with the highest star count receive the PP gain (via
     * {@link Player#applyRitualGain(int)}, which doubles the amount if the
     * double shaman bonus is active). The player(s) with the lowest star count
     * suffer the PP loss (via {@link Player#applyRitualLoss(int)}, which is a
     * no-op if PP protection is active). The event only fires during
     * {@link GamePhaseEnum#END_ROUND} or {@link GamePhaseEnum#PLAY_EVENT}.</p>
     *
     * @param players the list of all players; must not be {@code null}
     * @param phase   the current game phase
     */
    @Override
    public void execEvent(List<Player> players, GamePhaseEnum phase) {
        if (phase == GamePhaseEnum.END_ROUND || phase == GamePhaseEnum.PLAY_EVENT) {
            int maxStars = players.stream().mapToInt(Player::getNStars).max().getAsInt();
            int minStars = players.stream().mapToInt(Player::getNStars).min().getAsInt();

            for (Player player : players) {
                if (player.getNStars() == maxStars) {
                    player.applyRitualGain(ppGain);
                }
            }
            for (Player player : players) {
                if (player.getNStars() == minStars) {
                    player.applyRitualLoss(ppLoss);
                }
            }
        }
    }
}