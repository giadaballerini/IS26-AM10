package it.polimi.ingsw.client.ui.TUI.utility;

import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.TUI.ViewTUI;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.network.dto.PlayerDTO;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class TUICompleter implements Completer {

    private final ViewTUI tui;

    public TUICompleter(ViewTUI tui) {
        this.tui = tui;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        int wordIndex = line.wordIndex();
        List<String> words = line.words();

        if (wordIndex == 0) {
            completeCommands(candidates);
            return;
        }

        if (wordIndex == 1) {
            String command = words.getFirst().toLowerCase();
            completeArgument(command, candidates);
        }
    }


    private void completeCommands(List<Candidate> candidates) {
        VirtualModel model = tui.getModel();
        GamePhaseEnum phase = (model != null) ? model.getCurrentPhase() : null;

        candidates.add(new Candidate("help"));
        candidates.add(new Candidate("exit"));

        if (phase == null || phase == GamePhaseEnum.NONE) {
            candidates.add(new Candidate("create"));
            candidates.add(new Candidate("join"));
            candidates.add(new Candidate("choose"));
            candidates.add(new Candidate("ranking"));
        } else {
            candidates.add(new Candidate("info"));
            candidates.add(new Candidate("quit"));

            switch (phase) {
                case SETUP_PHASE:
                    candidates.add(new Candidate("move"));
                    break;

                case DRAW_PHASE: {
                    ActionsDTO actions = model.getToDoActions();
                    if (actions.getUpDraws() > 0 || actions.getDownDraws() > 0) {
                        candidates.add(new Candidate("draw"));
                    }
                    if (actions.isOptionalFlag()) {
                        candidates.add(new Candidate("skip"));
                    }
                    break;
                }

                case OPTIONAL_DRAW_PHASE: {
                    ActionsDTO actions = model.getToDoActions();
                    if (actions.getUpDraws() > 0 || actions.getDownDraws() > 0) {
                        candidates.add(new Candidate("draw"));
                    }
                    candidates.add(new Candidate("skip"));
                    break;
                }

                default:
                    break;
            }
        }
    }


    private void completeArgument(String command, List<Candidate> candidates) {
        VirtualModel model = tui.getModel();

        switch (command) {
            case "draw" -> completeDraw(model, candidates);
            case "move" -> completeMove(model, candidates);
            case "info" -> completeInfo(model, candidates);
            case "create" -> completeCreate(candidates);
        }
    }

    private void completeDraw(VirtualModel model, List<Candidate> candidates) {
        if (model == null) return;
        ActionsDTO actions = model.getToDoActions();

        if (actions.getUpDraws() > 0 && model.getUpperList() != null) {
            for (CardDTO card : model.getUpperList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (actions.getDownDraws() > 0 && model.getLowerList() != null) {
            for (CardDTO card : model.getLowerList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (actions.getUpDraws() == 0 && actions.getDownDraws() == 0) {
            if (model.getUpperList() != null) {
                for (CardDTO card : model.getUpperList()) {
                    candidates.add(new Candidate(String.valueOf(card.getId())));
                }
            }
            if (model.getLowerList() != null) {
                for (CardDTO card : model.getLowerList()) {
                    candidates.add(new Candidate(String.valueOf(card.getId())));
                }
            }
        }
    }

    private void completeMove(VirtualModel model, List<Candidate> candidates) {
        if (model == null || model.getBoard() == null) return;
        int i = 1;
        for (var tile : model.getBoard()) {
            if (!tile.isOccupied()) {
                candidates.add(new Candidate(String.valueOf(i)));
            }
            i++;
        }
    }

    private void completeInfo(VirtualModel model, List<Candidate> candidates) {
        if (model == null) return;

        if (model.getUpperList() != null) {
            for (CardDTO card : model.getUpperList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (model.getLowerList() != null) {
            for (CardDTO card : model.getLowerList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }

        if (model.getPlayers() != null) {
            for (PlayerDTO player : model.getPlayers()) {
                if (player.getMyBuildings() != null) {
                    for (CardDTO card : player.getMyBuildings()) {
                        candidates.add(new Candidate(String.valueOf(card.getId())));
                    }
                }
                if (player.getMyCharacters() != null) {
                    for (CardDTO card : player.getMyCharacters()) {
                        candidates.add(new Candidate(String.valueOf(card.getId())));
                    }
                }
            }
        }
    }

    private void completeCreate(List<Candidate> candidates) {
        for (int n = 2; n <= 5; n++) {
            candidates.add(new Candidate(String.valueOf(n)));
        }
    }
}