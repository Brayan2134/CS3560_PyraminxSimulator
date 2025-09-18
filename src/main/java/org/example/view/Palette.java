package org.example.view;

import javafx.scene.paint.Color;
import org.example.model.state.Face;

/** Simple face→color mapping for the renderer. */
public final class Palette {
    private Palette() {}
    public static Color color(Face f) {
        return switch (f) {
            case U -> Color.GOLD;     // pick any scheme you like
            case L -> Color.MEDIUMSEAGREEN;
            case R -> Color.LIGHTSKYBLUE;
            case B -> Color.TOMATO;
        };
    }
}
