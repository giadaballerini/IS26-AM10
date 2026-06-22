package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * JavaFX component that represents a game card in the GUI.
 * Extends {@link PopupPane} to provide a tooltip with the card's name and description on hover.
 * Each instance displays the card image (front or back), supports flipping,
 * an animated golden highlight for selectable cards, and a sliding animation
 * when the card belongs to the character row.
 */
public class CardGUI extends PopupPane {

    /** The unique identifier of the card this component represents. */
    private final int cardId;

    /** The type of the card, used to distinguish characters from buildings and events. */
    private final CardTypeEnum cardType;

    /** The image view displaying the card's front or back image. */
    private final ImageView view;

    /** The rectangle overlaid on the card image to render the selection highlight. */
    private final javafx.scene.shape.Rectangle highlight;

    /** The animation played on the highlight rectangle when the card is selectable. */
    private FadeTransition highlightAnim;

    /** Whether the card is currently displaying its front face. */
    private boolean isFaceUp = true;

    /**
     * Creates a new card component for the card with the given ID.
     * Loads the card's front image and sets up the highlight overlay.
     *
     * @param cardId the unique identifier of the card to display
     */
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

    /**
     * Returns the type of this card, used to distinguish characters from buildings and events.
     * @return the type of this card
     */
    public CardTypeEnum getType() {
        return cardType;
    }

    /**
     * Displays the back face of the card. Has no effect if the card is already face down.
     */
    public void showBack() {
        if (isFaceUp)
            flip();
    }

    /**
     * Displays the front face of the card. Has no effect if the card is already face up.
     */
    public void showFront() {
        if (!isFaceUp)
            flip();
    }

    /**
     * Flips the card to the opposite face, switching between front and back image.
     */
    public void flip() {
        isFaceUp = !isFaceUp;
        Image newImg = isFaceUp ?
                CardImagesLoader.getInstance().getFront(cardId) :
                CardImagesLoader.getInstance().getBack(cardId);
        view.setImage(newImg);
    }

    /**
     * Returns the unique identifier of the card this component represents.
     * @return the unique identifier of the card this component represents
     */
    public int getCardId() {
        return cardId;
    }

    /**
     * Shows or hides the animated selection highlight on this card.
     * When enabled, a pulsing golden border is displayed over the card image
     * to indicate that the card is selectable by the player.
     *
     * @param highlighted {@code true} to show the highlight, {@code false} to hide it
     */
    public void setHighlight(boolean highlighted) {
        if (highlighted) {
            highlight.setVisible(true);
            if (highlightAnim == null) {
                highlightAnim = new FadeTransition(Duration.millis(800), highlight);
                highlightAnim.setFromValue(0.3);
                highlightAnim.setToValue(1.0);
                highlightAnim.setCycleCount(FadeTransition.INDEFINITE);
                highlightAnim.setAutoReverse(true);
            }
            highlightAnim.play();
        } else {
            if (highlightAnim != null) highlightAnim.stop();
            highlight.setVisible(false);
        }
    }

    /**
     * Attaches mouse hover handlers that trigger a sliding animation when this card
     * is hovered in the character row. On hover, cards to the left slide further left,
     * this card slides right to become fully visible, and all cards in the building
     * and event row shift right to make room. On exit, all translations are reset.
     *
     * @param charRow    the {@link HBox} containing the character cards
     * @param buildEvRow the {@link HBox} containing the building and event cards
     */
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

                    for (Node child : buildEvRow.getChildren()) {
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

                for (Node child : buildEvRow.getChildren()) {
                    child.setTranslateX(0);
                }
            }
        });
    }
}