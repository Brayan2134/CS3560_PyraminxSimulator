package org.example.view;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import org.example.controller.PuzzleController;
import org.example.model.Move;

import java.util.List;

/**
 * Animator.java
 * Desc: Plays a sequence of moves with a fixed delay per move.
 * Role: UI helper (no per-frame rotation; just timed apply)
 */
public final class Animator {
    private Animator() {}

    public static void play(PuzzleController controller, List<Move> moves, Duration perMove) {
        SequentialTransition seq = new SequentialTransition();
        for (Move m : moves) {
            PauseTransition p = new PauseTransition(perMove);
            p.setOnFinished(e -> controller.apply(m));
            seq.getChildren().add(p);
        }
        seq.play();
    }
}
