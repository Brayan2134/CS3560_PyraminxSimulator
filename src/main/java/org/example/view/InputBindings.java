package org.example.view;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.example.controller.PuzzleController;
import org.example.model.state.Face;

/**
 * InputBindings.java
 * Desc: Keyboard controls for quick testing.
 * Role: Helper (UI input)
 *
 * Keys:
 *  - Uppercase: U/L/R/B = layer CW (turns=1)
 *  - Lowercase: u/l/r/b = tip CW (turns=1)
 *  - Shift modifies to CCW (turns=2)
 *  - Z = Undo, Y = Redo, S = Scramble, Backspace = Reset
 */
public final class InputBindings {
    private InputBindings() {}

    public static void install(Scene scene, PuzzleController controller) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            boolean ccw = e.isShiftDown(); // Shift -> 2 (CCW)
            int turns = ccw ? 2 : 1;

            KeyCode k = e.getCode();
            switch (k) {
                case U -> controller.layer(Face.U, turns);
                case L -> controller.layer(Face.L, turns);
                case R -> controller.layer(Face.R, turns);
                case B -> controller.layer(Face.B, turns);
                case Z -> controller.undo();
                case Y -> controller.redo();
                case S -> controller.scramble(12);
                case BACK_SPACE -> controller.reset();
                default -> {
                    // tips via typed char (lowercase)
                    char ch = e.getText() != null && !e.getText().isEmpty() ? e.getText().charAt(0) : '\0';
                    switch (ch) {
                        case 'u' -> controller.tip(Face.U, turns);
                        case 'l' -> controller.tip(Face.L, turns);
                        case 'r' -> controller.tip(Face.R, turns);
                        case 'b' -> controller.tip(Face.B, turns);
                    }
                }
            }
        });
    }
}
