package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to request the global ranking leaderboard from the server.
 *
 * <p>Only valid after the game has ended. The server responds with a
 * {@link it.polimi.ingsw.network.messages.server.RankingResponseMessage}.
 */
public class RankingRequestMessage implements ClientMessage {

    /** Nickname of the player requesting the ranking leaderboard. */
    private final String nickname;

    /**
     * Creates a {@code RankingRequestMessage} for the given player.
     *
     * @param nickname nickname of the requesting player
     */
    public RankingRequestMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the nickname of the player requesting the ranking leaderboard.
     *
     * @return player nickname
     */
    public String getNickname() {
        return this.nickname;
    }
}