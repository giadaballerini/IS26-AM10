package it.polimi.ingsw.client.ui.gui;

import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

/**
 * Utility class that manages custom cursors across the GUI.
 * Provides a base cursor for general navigation and a hover cursor
 * that is automatically applied when the mouse moves over interactive elements
 * such as cards, tiles, buttons, and text fields.
 * Cursors are loaded lazily on first use and shared across the application.
 */
public class CursorManager {

    /** The default cursor displayed during general navigation. */
    private static ImageCursor baseCursor;

    /** The cursor displayed when hovering over interactive elements. */
    private static ImageCursor hoverCursor;

    /**
     * Loads the base and hover cursor images if they have not been loaded yet.
     * Silently skips initialization if the image resources are not found.
     */
    private static void initCursors() {
        if (baseCursor == null || hoverCursor == null) {
            try {
                baseCursor = new ImageCursor(new Image(Objects.requireNonNull(Objects.requireNonNull(CursorManager.class.getResourceAsStream("/images/cursor_arrowhead_128.png")))), 0, 0);
                hoverCursor = new ImageCursor(new Image(Objects.requireNonNull(CursorManager.class.getResourceAsStream("/images/cursor_32.png"))), 3, 1);
            } catch (Exception e) {
                System.out.println("Immagini cursore non trovate.");
            }
        }
    }

    /**
     * Applies the base cursor to the given scene.
     *
     * @param scene the scene to apply the base cursor to
     */
    public static void applyBaseCursor(Scene scene) {
        initCursors();
        if (baseCursor != null) {
            scene.setCursor(baseCursor);
        }
    }

    /**
     * Sets the hover cursor on the given nodes, overriding their default cursor.
     *
     * @param nodes the nodes to apply the hover cursor to
     */
    public static void makeNodesHoverable(Node... nodes) {
        initCursors();
        if (hoverCursor != null) {
            for (Node n : nodes) {
                n.setCursor(hoverCursor);
            }
        }
    }

    /**
     * Determines whether the given node or any of its ancestors is considered
     * interactive, by checking for the CSS style classes {@code button},
     * {@code toggle-button}, {@code text-field}, {@code table-row-cell},
     * {@code card}, and {@code tile}.
     *
     * @param node the node to check
     * @return {@code true} if the node or one of its ancestors is interactive,
     *         {@code false} otherwise
     */
    private static boolean isHoverable(Node node) {
        Node current = node;
        while (current != null) {
            for (String styleClass : current.getStyleClass()) {
                if (styleClass.equals("button") ||
                        styleClass.equals("toggle-button") ||
                        styleClass.equals("text-field") ||
                        styleClass.equals("table-row-cell") ||
                        styleClass.equals("card") ||
                        styleClass.equals("tile")) {
                    return true;
                }
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Registers a mouse-move filter on the given scene that automatically switches
     * between the hover cursor and the base cursor depending on whether the node
     * under the pointer is interactive.
     *
     * @param scene the scene to apply the dynamic cursor switching to
     */
    public static void applyHoverToScene(Scene scene) {
        initCursors();
        if (hoverCursor == null) return;
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            Node target = (Node) e.getTarget();
            if (isHoverable(target)) {
                scene.setCursor(hoverCursor);
            } else {
                scene.setCursor(baseCursor);
            }
        });
    }
    /** Prevents instantiation of this utility class; cursors are accessed through static methods only. */
    private CursorManager() {}
}