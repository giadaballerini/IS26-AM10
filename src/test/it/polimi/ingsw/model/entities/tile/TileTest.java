package it.polimi.ingsw.model.entities.tile;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TileTest {

    private Tile tile(int id) {
        return new Tile(id, 2, null, null, false, "");
    }

    @Test
    void testShouldSetPlayer() {
        Tile tile = tile(1);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.setPlayer(player);
        assertEquals(player, tile.getPlayer());
    }

    @Test
    void testShouldGetPlayer() {
        Tile tile = tile(2);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.setPlayer(player);
        assertEquals(player, tile.getPlayer());
    }

    @Test
    void testShouldIsOccupied() {
        Tile tile = tile(1);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        assertFalse(tile.isOccupied());
        tile.setPlayer(player);
        assertTrue(tile.isOccupied());
    }

    @Test
    void testShouldGetMinPlayers() {
        assertEquals(2, tile(2).getMinPlayers());
    }

    @Test
    void testShouldGetId() {
        assertEquals(1, tile(1).getId());
    }

    @Test
    void testShouldRemovePlayer() {
        Tile tile = tile(2);
        tile.removePlayer();
        assertNull(tile.getPlayer());
        assertFalse(tile.isOccupied());
    }

    @Test
    void testShouldExecInteractiveEffect() {
        Player mockPlayer = mock(Player.class);
        DrawCard mockEffect1 = mock(DrawCard.class);
        DrawCard mockEffect2 = mock(DrawCard.class);
        Action mockAction1 = mock(Action.class);
        Action mockAction2 = mock(Action.class);

        Tile sut = spy(new Tile(3, 2, new ArrayList<>(), List.of(mockEffect1, mockEffect2), false, ""));
        doReturn(mockPlayer).when(sut).getPlayer();
        when(mockEffect1.apply(mockPlayer)).thenReturn(mockAction1);
        when(mockEffect2.apply(mockPlayer)).thenReturn(mockAction2);

        List<Action> result = sut.execInteractiveEffect();

        assertEquals(2, result.size());
        assertEquals(mockAction1, result.get(0));
        assertEquals(mockAction2, result.get(1));
        verify(mockEffect1).apply(mockPlayer);
        verify(mockEffect2).apply(mockPlayer);
    }

    @Test
    void testShouldGetPlayerNickname(){
        Tile tile = tile(1);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.setPlayer(player);
        assertEquals("Player", tile.getPlayerNickname());
        tile.removePlayer();
        assertEquals("", tile.getPlayerNickname());
    }
}