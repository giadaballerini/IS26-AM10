package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.client.VirtualModel;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class TuiCompleter implements Completer {
    private final ViewTUI tui;

    public TuiCompleter(ViewTUI tui) {
        this.tui = tui;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        int wordIndex = line.wordIndex();
        List<String> words = line.words();
        if (wordIndex == 0) {
            candidates.add(new Candidate("draw"));
            candidates.add(new Candidate("exit"));
            candidates.add(new Candidate("skip"));
            candidates.add(new Candidate("move"));
            return;
        }
        if (wordIndex == 1) {
            String command = words.getFirst().toLowerCase();
            if (command.equals("draw")) {
                VirtualModel model = tui.getModel();
                if (model == null) return;
                if (model.getUpperList() != null) {
                    for (int i = 0; i < model.getUpperList().size(); i++) {
                        String id = String.valueOf(model.getUpperList().get(i).getId());
                        candidates.add(new Candidate(id));
                    }
                }
                if (model.getLowerList() != null) {
                    for (int i = 0; i < model.getLowerList().size(); i++) {
                        String id = String.valueOf(model.getLowerList().get(i).getId());
                        candidates.add(new Candidate(id));
                    }
                }
            }
            if (command.equals("move")) {
                VirtualModel model = tui.getModel();
                if (model == null) return;
                for (int i = 0; i < model.getBoard().size(); i++) {
                    String id = String.valueOf(model.getBoard().get(i).getId());
                    if (!model.getBoard().get(i).isOccupied()) {
                        candidates.add(new Candidate(id));
                    }
                }
            }
        }
    }
}
