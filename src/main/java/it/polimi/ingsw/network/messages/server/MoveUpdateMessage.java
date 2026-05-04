package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.TileDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class MoveUpdateMessage implements ServerMessage {

    private final TileDTO tile;
    private final String nickname;

    public MoveUpdateMessage(TileDTO tile, String nickname) {
        this.tile = tile;
        this.nickname = nickname;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public TileDTO getTile() {
        return tile;
    }

    public String getNickname() {
        return nickname;
    }
}
