package org.example.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.example.model.Move;
import org.example.model.PyraminxState;
import org.example.model.engine.History;
import org.example.model.moves.LayerRotation;
import org.example.model.moves.MoveLibrary;
import org.example.model.moves.TipRotation;
import org.example.model.state.Face;

import java.util.List;

/**
 * PuzzleController.java
 * Desc: Thin glue: user intent -> model updates. Exposes observable state.
 * Role: Core controller (no rendering)
 *
 * Invariants:
 *  - State is immutable snapshots; property updated on every change.
 */
public final class PuzzleController {
    private final History history;
    private final ObjectProperty<PyraminxState> state = new SimpleObjectProperty<>(PyraminxState.solved());

    public PuzzleController(History history) { this.history = history; }

    /** Read-only state for the View to observe. */
    public ObjectProperty<PyraminxState> stateProperty() { return state; }

    /** Apply any move and publish state. */
    public void apply(Move m) { state.set(history.apply(state.get(), m)); }

    /** Convenience: layer/tip moves. */
    public void layer(Face f, int turns) { apply(new LayerRotation(f, turns)); }
    public void tip(Face f, int turns)   { apply(new TipRotation(f, turns)); }

    /** Parser hook (later useful for an input field). */
    public void applyAlg(String alg) {
        List<Move> seq = MoveLibrary.parse(alg);
        PyraminxState s = state.get();
        for (Move m : seq) s = history.apply(s, m);
        state.set(s);
    }

    /** History ops. */
    public void undo() { state.set(history.undo(state.get())); }
    public void redo() { state.set(history.redo(state.get())); }
    public void reset(){ history.clear(); state.set(PyraminxState.solved()); }

    /** Scramble N random moves. */
    public void scramble(int n) {
        PyraminxState s = state.get();
        for (Move m : MoveLibrary.scramble(n)) s = history.apply(s, m);
        state.set(s);
    }

    /**
     * Undoes all applied moves (fast "solve" path using history).
     * Role: Core controller convenience
     *
     * Postconditions:
     *  - State becomes the result of undoing every move in the undo stack.
     */
    public void solveByUndoAll() {
        PyraminxState s = state.get();
        while (history.undoSize() > 0) s = history.undo(s);
        state.set(s);
    }

    /** Applies a list of moves as one transaction (no animation). */
    public void applyAll(java.util.List<Move> seq) {
        PyraminxState s = state.get();
        for (Move m : seq) s = history.apply(s, m);
        state.set(s);
    }

}
