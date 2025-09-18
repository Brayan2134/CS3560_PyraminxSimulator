package org.example.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.model.PyraminxState;
import org.example.model.state.CenterPos;
import org.example.model.state.Face;

/**
 * FxRenderer.java
 * Desc: Minimal diagnostic renderer: shows four face panels with center orientation.
 * Role: View (read-only)
 *
 * Postconditions:
 *  - Repaints whenever the observed state changes.
 *  - Safe to replace later with proper triangular stickers.
 */
public final class FxRenderer extends Pane {
    private final ReadOnlyObjectProperty<PyraminxState> state;

    public FxRenderer(ReadOnlyObjectProperty<PyraminxState> state) {
        this.state = state;
        setPadding(new Insets(12));
        setMinSize(600, 400);
        // initial paint + subscribe
        repaint(state.get());
        state.addListener((obs, oldS, newS) -> repaint(newS));
    }

    private void repaint(PyraminxState s) {
        getChildren().clear();
        Group g = new Group();

        // Layout 2x2 panels for U/L/R/B
        double w = 140, h = 100, gap = 16;
        addPanel(g, "U", Color.LIGHTYELLOW, 0, 0, s.centerOrientation(CenterPos.U));
        addPanel(g, "L", Color.LIGHTGREEN, 0, h + gap, s.centerOrientation(CenterPos.L));
        addPanel(g, "R", Color.LIGHTBLUE,  w + gap, h + gap, s.centerOrientation(CenterPos.R));
        addPanel(g, "B", Color.LIGHTSALMON, w + gap, 0, s.centerOrientation(CenterPos.B));

        getChildren().add(g);
    }

    private void addPanel(Group g, String name, Color color, double x, double y, int centerOri) {
        Rectangle r = new Rectangle(x, y, 140, 100);
        r.setArcWidth(16); r.setArcHeight(16);
        r.setFill(color); r.setStroke(Color.GRAY);

        Label title = new Label(name);
        title.setLayoutX(x + 8); title.setLayoutY(y + 6);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label ori = new Label("center ori = " + centerOri + " (mod 3)");
        ori.setLayoutX(x + 8); ori.setLayoutY(y + 34);

        g.getChildren().addAll(r, title, ori);
    }
}
