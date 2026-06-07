package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;


public class CardGUI extends PopupPane {
    private final int cardId;
    private final CardTypeEnum cardType;
    private final ImageView view;
    private final javafx.scene.shape.Rectangle highlight;
    private FadeTransition highlightAnim;
    private boolean isFaceUp = true;

    public CardGUI(int cardId) {
        super(CardRegistry.getName(cardId), CardRegistry.getDescription(cardId));
        this.cardId = cardId;
        this.cardType = CardRegistry.getType(cardId);
        this.view = new ImageView();

        view.setImage(CardImagesLoader.getInstance().getFront(cardId));

        view.setFitWidth(110);
        view.setPreserveRatio(true);
        view.setSmooth(true);


        highlight = new javafx.scene.shape.Rectangle(110, 165);
        highlight.setFill(javafx.scene.paint.Color.TRANSPARENT);
        highlight.setArcWidth(8);
        highlight.setArcHeight(8);
        highlight.setVisible(false);
        highlight.getStyleClass().add("card-highlight");
        highlight.setMouseTransparent(true);

        this.getChildren().addAll(view, highlight);

        this.getStyleClass().add("card");

    }

    public CardTypeEnum getType() {
        return cardType;
    }


    public void showBack(){
        if(isFaceUp)
            flip();
    }

    public void showFront(){
        if(!isFaceUp)
            flip();
    }

    public void flip() {
        isFaceUp = !isFaceUp;
        Image newImg = isFaceUp ?
                CardImagesLoader.getInstance().getFront(cardId) :
                CardImagesLoader.getInstance().getBack(cardId);
        view.setImage(newImg);
    }

    public int getCardId(){
        return cardId;
    }

    public void setHighlight(boolean highlighted) {
        if (highlighted) {
            highlight.setVisible(true);
            if(highlightAnim == null){
                highlightAnim = new FadeTransition(Duration.millis(800), highlight);
                highlightAnim.setFromValue(0.3);
                highlightAnim.setToValue(1.0);
                highlightAnim.setCycleCount(FadeTransition.INDEFINITE);
                highlightAnim.setAutoReverse(true);
            }
            highlightAnim.play();
        } else{
            if(highlightAnim != null) highlightAnim.stop();
            highlight.setVisible(false);
        }
    }
    public void showSliding(HBox charRow, HBox buildEvRow) {
        var oldMouseEntered = this.getOnMouseEntered();
        var oldMouseExited = this.getOnMouseExited();

        this.setOnMouseEntered(event -> {
            if (oldMouseEntered != null) {
                oldMouseEntered.handle(event);
            }

            if (this.getParent() == charRow) {
                int idx = charRow.getChildren().indexOf(this);

                if (idx > 0) {
                    for (int i = 0; i < idx; i++) {
                        charRow.getChildren().get(i).setTranslateX(-30);
                    }

                    this.setTranslateX(30);


                    for (int i = idx + 1; i < charRow.getChildren().size(); i++) {
                        charRow.getChildren().get(i).setTranslateX(90);
                    }

                    for(Node child : buildEvRow.getChildren()){
                        child.setTranslateX(90);
                    }
                }
            }
        });

        this.setOnMouseExited(event -> {
            if (oldMouseExited != null) {
                oldMouseExited.handle(event);
            }

            if (this.getParent() == charRow) {

                for (Node child : charRow.getChildren()) {
                    int childIdx = charRow.getChildren().indexOf(child);
                    child.setViewOrder(childIdx);
                    child.setTranslateX(0);
                }
                this.setViewOrder(charRow.getChildren().indexOf(this) - 0.1);

                for(Node child : buildEvRow.getChildren()){
                    child.setTranslateX(0);
                }
            }
        });
    }
}