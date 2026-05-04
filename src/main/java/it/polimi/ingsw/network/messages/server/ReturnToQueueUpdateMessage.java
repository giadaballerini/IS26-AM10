package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.TileDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class ReturnToQueueUpdateMessage implements ServerMessage {
    private final TileDTO tileDTO;
    private final PlayerStatsDTO playerStatsDTO;
    public ReturnToQueueUpdateMessage(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        this.tileDTO = tileDTO;
        this.playerStatsDTO = playerStatsDTO;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public TileDTO getTileDTO() {
        return tileDTO;
    }
    public PlayerStatsDTO getPlayerStatsDTO() {
        return playerStatsDTO;
    }
}
