package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client when the player draws a card.
 *
 * <p>The server validates the draw and broadcasts a
 * {@link it.polimi.ingsw.network.messages.server.DrawUpdateMessage} to all
 * clients if accepted.
 */
public class DrawMessage implements ClientMessage {

    /** Index identifying which card the player draws. */
    private final int cardId;

    /** Nickname of the player drawing the card. */
    private final String nickname;

    /**
     * Creates a {@code DrawMessage} for the given player and card index.
     *
     * @param cardId   index of the card to draw
     * @param nickname nickname of the drawing player
     */
    public DrawMessage(int cardId, String nickname) {
        this.cardId = cardId;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the index of the card to draw.
     *
     * @return card index
     */
    public int getCardId() {
        return cardId;
    }

    /**
     * Returns the nickname of the drawing player.
     *
     * @return player nickname
     */
    public String getNickname() {
        return nickname;
    }
}