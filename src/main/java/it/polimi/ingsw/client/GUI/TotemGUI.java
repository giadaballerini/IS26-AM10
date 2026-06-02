package it.polimi.ingsw.client.GUI;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class TotemGUI extends StackPane {
    private final ImageView totem;

    public TotemGUI(ColorPawnEnum color) {
        totem = new ImageView();
        totem.setImage(PawnImagesLoader.getInstance().getTotemImage(color));
        totem.setPreserveRatio(true);
        totem.setPickOnBounds(true);
        totem.setFitHeight(60);
        totem.setFitWidth(60);

        getChildren().add(totem);
    }

}
