package it.polimi.ingsw.client.GUI;

import it.polimi.ingsw.client.QTileData;
import it.polimi.ingsw.client.QTileRegistry;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class QTileGUI extends PopupPane {

    private final QTileData qTileData;
    private final ImageView view;
    private final AnchorPane anchorPane;
    private final Map<Integer, Boolean> posToOccupied = new HashMap<>();

    public QTileGUI(int qTileId) {

    super(QTileRegistry.getName(), QTileRegistry.getDescription(qTileId));

        this.qTileData = QTileRegistry.getTile(qTileId);
        for(int i = 0; i < qTileId; i++){
            posToOccupied.put(i, false);
        }
        view = new ImageView();
        view.setImage(QTileImagesLoader.getInstance().getFront(qTileId));
        view.setFitWidth(110);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        anchorPane = new AnchorPane();

        getChildren().addAll(view, anchorPane);
        this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");
        this.getStyleClass().add("tile");
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            this.anchorPane.setOpacity(0.4);
        });
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            this.anchorPane.setOpacity(1);
        });

    }


    public void addPawn (TotemGUI pawn){

        int firstFreeSlot = -1;
        for(int j = 0; j < qTileData.getId(); j++) {
            if (!posToOccupied.get(j)) {
                firstFreeSlot = j;
                break;
            }
        }
        if(firstFreeSlot != -1) {
            anchorPane.getChildren().add(pawn);
            anchorPane.setTopAnchor(pawn, qTileData.getSlotY(firstFreeSlot));
            anchorPane.setLeftAnchor(pawn, qTileData.getSlotX(firstFreeSlot));
            posToOccupied.put(firstFreeSlot, true);
        }
    }

    public void removePawn(TotemGUI pawn){
        anchorPane.getChildren().remove(pawn);
        for(int j = 0; j < qTileData.getId(); j++) {
            if (posToOccupied.get(j)) {
                posToOccupied.put(j, false);
                break;
            }
        }
    }


    public void clearPawns() {
        anchorPane.getChildren().clear();
        for (int i = 0; i < qTileData.getId(); i++) {
            posToOccupied.put(i, false);
        }
    }
}
