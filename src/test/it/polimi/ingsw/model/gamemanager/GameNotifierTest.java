package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameNotifierTest {

    @Mock ModelObserver observer;
    @Mock Player mockPlayer;
    @Mock GamePhaseState mockPhaseState;

    GameNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new GameNotifier(List.of(observer));
    }


    @Test
    void notifyCurrPlayerUpdate() {
        notifier.notifyCurrPlayerUpdate("alice");

        verify(observer).onCurrPlayerUpdate("alice");
    }


    @Test
    void notifyMoveUpdate() {
        TileDTO tile = new TileDTO(true, 1, 2, "alice", 1, 1, 3);

        notifier.notifyMoveUpdate(tile, "alice");

        verify(observer).onMoveUpdate(tile, "alice");
    }


    @Test
    void notifyPhaseUpdate() {
        when(mockPhaseState.getPhase()).thenReturn(GamePhaseEnum.DRAW_PHASE);

        notifier.notifyPhaseUpdate(mockPhaseState);


        verify(observer).onPhaseUpdate(any(PhaseDTO.class));
    }


    @Test
    void notifyGameEnding() {
        when(observer.getNickname()).thenReturn("alice");

        PlayerStatsDTO stats = new PlayerStatsDTO("alice", 10, 5, 3, 1, 0);
        List<PlayerStatsDTO> statsList = List.of(stats);
        Map<String, Integer> globalPositions  = Map.of("alice", 1);
        Map<String, Integer> rankingPositions = Map.of("alice", 2);

        notifier.notifyGameEnding(statsList, globalPositions, rankingPositions);

        verify(observer).onGameEnding(statsList, 2, 1);
    }

    @Test
    void notifyGameEnding_observerNotInMap_usesDefaultMinusOne() {
        when(observer.getNickname()).thenReturn("bob");

        List<PlayerStatsDTO> statsList = List.of();
        notifier.notifyGameEnding(statsList, Map.of(), Map.of());

        verify(observer).onGameEnding(statsList, -1, -1);
    }


    @Test
    void notifyDrawUpdate() {
        it.polimi.ingsw.model.entities.card.Card mockCard =
                mock(it.polimi.ingsw.model.entities.card.Card.class);
        CardDTO cardDTO = new CardDTO(1, 1, CardTypeEnum.GATHERER);
        when(mockCard.toDTO()).thenReturn(cardDTO);
        when(mockPlayer.getNickname()).thenReturn("alice");

        notifier.notifyDrawUpdate(mockPlayer, mockCard);

        verify(observer).onDrawUpdate(cardDTO, "alice");
    }


    @Test
    void notifyStatusUpdate() {
        PlayerStatusDTO statusDTO = new PlayerStatusDTO("alice");
        when(mockPlayer.toStatusDTO()).thenReturn(statusDTO);

        notifier.notifyStatusUpdate(mockPlayer);

        verify(observer).onStatusUpdate(statusDTO);
    }


    @Test
    void notifyStatsUpdate() {
        PlayerStatsDTO statsDTO = new PlayerStatsDTO("alice", 5, 3, 2, 0, 0);
        when(mockPlayer.toStatsDTO()).thenReturn(statsDTO);

        notifier.notifyStatsUpdate(mockPlayer);

        verify(observer).onStatsUpdate(statsDTO);
    }


    @Test
    void notifyEventUpdate() {
        EventDTO eventDTO = new EventDTO();

        notifier.notifyEventUpdate(eventDTO);

        verify(observer).onEvent(eventDTO);
    }


    @Test
    void showBoard() {
        BoardDTO boardDTO = new BoardDTO(
                List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(),
                "alice", new ActionsDTO(1, 1, false),
                GamePhaseEnum.DRAW_PHASE, 1, 2, 10);

        notifier.showBoard(boardDTO);

        verify(observer).showBoard(boardDTO);
    }



    @Test
    void notifySkip() {
        when(mockPlayer.getNickname()).thenReturn("alice");

        notifier.notifySkip(mockPlayer);

        verify(observer).notifySkip("alice");
    }



    @Test
    void notifyDrawable_currPlayerReceivesRealDTO() {
        when(observer.getNickname()).thenReturn("alice");
        ActionsDTO dto = new ActionsDTO(2, 1, true);

        notifier.notifyDrawable(dto, "alice");

        verify(observer).notifyDrawable(dto);
    }

    @Test
    void notifyDrawable_otherPlayerReceivesEmptyDTO() {
        when(observer.getNickname()).thenReturn("bob");
        ActionsDTO dto = new ActionsDTO(2, 1, true);

        notifier.notifyDrawable(dto, "alice");

        verify(observer).notifyDrawable(argThat(a -> !a.isSkippable()));
    }


    @Test
    void notifyReturnToQueue() {
        PlayerStatsDTO statsDTO = new PlayerStatsDTO("alice", 5, 3, 2, 0, 0);
        TileDTO tileDTO = new TileDTO(false, 1, 2, null, 1, 1, 3);

        notifier.notifyReturnToQueue(statsDTO, tileDTO);

        verify(observer).onReturnToQueue(tileDTO, statsDTO);
    }


    @Test
    void notifyChangeAge() {
        ChangeAgeDTO ageDTO = new ChangeAgeDTO(List.of(), List.of(), 2, 50);

        notifier.notifyChangeAge(ageDTO);

        verify(observer).onChangeAge(ageDTO);
    }
}