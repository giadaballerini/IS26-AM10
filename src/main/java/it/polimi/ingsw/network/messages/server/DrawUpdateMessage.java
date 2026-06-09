package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when a player draws a card.
 */
public class DrawUpdateMessage implements ServerMessage {

    /** The card that was drawn. */
    private final CardDTO c;

    /** Nickname of the player who drew the card. */
    private final String nickname;

    /**
     * Creates a {@code DrawUpdateMessage} for the given card and player.
     *
     * @param c        the drawn card
     * @param nickname nickname of the drawing player
     */
    public DrawUpdateMessage(CardDTO c, String nickname) {
        this.c = c;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the card that was drawn.
     *
     * @return drawn card DTO
     */
    public CardDTO getCardDTO() {
        return c;
    }

    /**
     * Returns the nickname of the player who drew the card.
     *
     * @return drawing player's nickname
     */
    public String getNickname() {
        return nickname;
    }
}