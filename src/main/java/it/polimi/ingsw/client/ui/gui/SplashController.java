package it.polimi.ingsw.client.ui.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * FXML controller for the splash screen shown during application startup.
 *
 * <p>Displayed while the GUI bootstraps its resources (tile images, card images,
 * pawn images, etc. via {@link QTileImagesLoader}, {@link CardImagesLoader},
 * {@link TileImagesLoader} and {@link PawnImagesLoader}). Runs a looping
 * dot animation on a loading label to give the user visual feedback that
 * the application is working.</p>
 *
 * <p>Once loading is complete, {@link #stopAnimation()} should be called
 * before transitioning to the next scene via {@link SceneManager}.</p>
 */
public class SplashController {

    /**
     * The label displaying the animated loading message.
     * Injected from the corresponding FXML layout file.
     */
    @FXML
    private Label loadingLabel;

    /** The timeline driving the dot cycling animation on {@link #loadingLabel}. */
    private Timeline dotsAnim;

    /**
     * Initializes the splash screen by starting the loading dot animation.
     *
     * <p>Called automatically by the JavaFX framework after the FXML fields
     * have been injected. Sets up a {@link Timeline} that cycles the label
     * text through {@code "."}, {@code ".."}, {@code "..."} every 500 ms
     * indefinitely, until {@link #stopAnimation()} is called.</p>
     */
    @FXML
    public void initialize() {
        String[] dots = {".", "..", "..."};
        int[] i = {0};
        dotsAnim = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    i[0] = (i[0] + 1) % dots.length;
                    loadingLabel.setText("Caricamento risorse" + dots[i[0]]);
                })
        );
        dotsAnim.setCycleCount(Timeline.INDEFINITE);
        dotsAnim.play();
    }

    /**
     * Stops the dot animation when resource loading is complete.
     *
     * <p>Should be called by the application launcher
     * just before switching away from the splash screen via
     * {@link SceneManager#switchTo(String)}. Safe to call even if
     * {@link #initialize()} has not run yet.</p>
     */
    public void stopAnimation() {
        if (dotsAnim != null) dotsAnim.stop();
    }
    /**
     * Creates a new {@code SplashController} instance.
     * Called by the JavaFX {@code FXMLLoader} via reflection.
     */
    public SplashController() {
    }
}