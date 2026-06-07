package it.polimi.ingsw.client.ui.GUI;

import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class CursorManager {
    private static ImageCursor baseCursor;
    private static ImageCursor hoverCursor;

    private static void initCursors() {
        if (baseCursor == null || hoverCursor == null) {
            try {
                baseCursor = new ImageCursor(new Image(CursorManager.class.getResourceAsStream("/images/cursor_arrowhead_128.png")), 0, 0);
                hoverCursor = new ImageCursor(new Image(CursorManager.class.getResourceAsStream("/images/cursor_32.png")), 3, 1);
            } catch (Exception e) {
                System.out.println("Immagini cursore non trovate.");
            }
        }
    }

    public static void applyBaseCursor(Scene scene) {
        initCursors();
        if (baseCursor != null) {
            scene.setCursor(baseCursor);
        }
    }

    public static void makeNodesHoverable(Node... nodes) {
        initCursors();
        if (hoverCursor != null) {
            for (Node n : nodes) {
                n.setCursor(hoverCursor);
            }
        }
    }

    public static ImageCursor getHoverCursor() {
        initCursors();
        return hoverCursor;
    }

    public static ImageCursor getBaseCursor() {
        initCursors();
        return baseCursor;
    }

    private static boolean isHoverable(Node node) {
        Node current = node;
        while (current != null) {
            for (String styleClass : current.getStyleClass()) {
                if (styleClass.equals("button") ||
                        styleClass.equals("toggle-button") ||
                        styleClass.equals("text-field") ||
                        styleClass.equals("table-row-cell") || styleClass.equals("card") || styleClass.equals("tile")) {
                    return true;
                }
            }
            current = current.getParent();
        }
        return false;
    }

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
}