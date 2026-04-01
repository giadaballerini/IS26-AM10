package it.polimi.ingsw.model.entities.tile;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TileTest {

    @Mock
    Action mockAction;

    @Test
    void testShouldSetPlayer(){
        Tile tile = new Tile('A', 2, null, null);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.setPlayer(player);
        assertEquals(player, tile.getPlayer());
    }

    @Test
    void testShouldGetPlayer(){
        Tile tile = new Tile('A', 2, null, null);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.setPlayer(player);
        assertEquals(player, tile.getPlayer());
    }

    @Test
    void testShouldIsOccupied(){
        Tile tile = new Tile('A', 2, null, null);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        assertFalse(tile.isOccupied());
        tile.setPlayer(player);
        assertTrue(tile.isOccupied());
    }

    @Test
    void testShouldGetMinPlayers(){
        Tile tile = new Tile('A', 2, null, null);
        assertEquals(2, tile.getMinPlayers());
    }

    @Test
    void testShouldGetId(){
        Tile tile = new Tile('A', 2, null, null);
        assertEquals('A', tile.getId());
    }

    @Test
    void testShouldRemovePlayer(){
        Tile tile = new Tile('A', 2, null, null);
        Player player = new Player("Player", ColorPawnEnum.BLUE);
        tile.removePlayer();
        assertNull(tile.getPlayer());
        assertFalse(tile.isOccupied());
    }

    @Test
    void testShouldExecInteractiveEffect(){
        Player mockPlayer = mock(Player.class);
        DrawCard mockEffect1 = mock(DrawCard.class);
        DrawCard mockEffect2 = mock(DrawCard.class);
        Action mockAction1 = mock(Action.class);
        Action mockAction2 = mock(Action.class);

        Tile sut = spy(new Tile('A', 2, new ArrayList<>(), Arrays.asList(mockEffect1, mockEffect2)));

        doReturn(mockPlayer).when(sut).getPlayer();

        when(mockEffect1.apply(mockPlayer)).thenReturn(mockAction1);
        when(mockEffect2.apply(mockPlayer)).thenReturn(mockAction2);

        List<Action> result = sut.execInteractiveEffect();

        assertEquals(2, result.size(), "La lista deve contenere esattamente 2 azioni");

        assertEquals(mockAction1, result.get(0));
        assertEquals(mockAction2, result.get(1));

        verify(mockEffect1, times(1)).apply(mockPlayer);
        verify(mockEffect2, times(1)).apply(mockPlayer);

    }
}