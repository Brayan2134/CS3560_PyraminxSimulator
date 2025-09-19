package org.example.controller;

import javafx.beans.property.*;
import org.example.model.Move;
import org.example.model.PyraminxState;
import org.example.model.engine.History;
import org.example.model.moves.LayerRotation;
import org.example.model.moves.MoveLibrary;
import org.example.model.moves.TipRotation;
import org.example.model.state.Face;
import org.example.io.GameIO;

import java.util.List;
import java.nio.file.Path;

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

    private final StringProperty algText = new SimpleStringProperty("");
    private final IntegerProperty moveCount = new SimpleIntegerProperty(0);

    /** Autosave target directory (user-scoped). */
    private final Path autosaveDir = Path.of(System.getProperty("user.home"), ".pyraminx");

    public PuzzleController(History history) { this.history = history; }

    /** Read-only state for the View to observe. */
    public ObjectProperty<PyraminxState> stateProperty() { return state; }
    public ReadOnlyStringProperty algTextProperty() { return algText; }
    public ReadOnlyIntegerProperty moveCountProperty() { return moveCount; }

    /** Internal: refresh derived UI properties from history. */
    private void refreshMeta() {
        algText.set(history.toAlg());
        moveCount.set(history.undoSize());
    }

    /**
     * autosaveNow()
     * Desc: Writes current state snapshot to autosave file.
     * Role: Persistence (best-effort, non-blocking)
     *
     * Preconditions: state != null
     * Postconditions: ~/.pyraminx/pyraminx.save updated on success.
     */
    private void autosaveNow() {
        try {
            GameIO.autosave(autosaveDir, state.get());
        } catch (Exception ignored) {
            // best-effort: swallow to avoid UI noise; next change will retry
        }
    }

    /**
     * replaceState(...)
     * Desc: Replace entire puzzle state (e.g., on load).
     * Role: Controller boundary for external loads
     *
     * Preconditions: s != null (already validated)
     * Postconditions: history cleared, state set, meta refreshed, autosaved.
     */
    public void replaceState(PyraminxState s) {
        history.clear();
        state.set(s);
        refreshMeta();   // keep meta in sync first
        autosaveNow();   // then persist
    }

    /**
     * initFromAutosaveOrSolved()
     * Desc: Load autosave if valid; else start from solved.
     */
    public void initFromAutosaveOrSolved() {
        PyraminxState loaded = GameIO.loadOrDefault(autosaveDir, PyraminxState::solved);
        replaceState(loaded);
    }

    /** Apply any move and publish state + meta. */
    public void apply(Move m) {
        state.set(history.apply(state.get(), m));
        refreshMeta();
        autosaveNow();
    }

    /** History ops. */
    public void undo() {
        state.set(history.undo(state.get()));
        refreshMeta();
        autosaveNow();
    }
    public void redo() {
        state.set(history.redo(state.get()));
        refreshMeta();
        autosaveNow();
    }
    public void reset() {
        history.clear();
        state.set(PyraminxState.solved());
        refreshMeta();
        autosaveNow();
    }


    /** Convenience: layer/tip moves. */
    public void layer(Face f, int turns) { apply(new LayerRotation(f, turns)); }
    public void tip(Face f, int turns)   { apply(new TipRotation(f, turns)); }

    /** Parser hook (later useful for an input field). */
    public void applyAlg(String alg) {
        List<Move> seq = MoveLibrary.parse(alg);
        PyraminxState s = state.get();
        for (Move m : seq) s = history.apply(s, m);
        state.set(s);
        refreshMeta();
        autosaveNow();
    }

    /** Scramble N random moves. */
    public void scramble(int n) {
        PyraminxState s = state.get();
        for (Move m : MoveLibrary.scramble(n)) s = history.apply(s, m);
        state.set(s);
        refreshMeta();
        autosaveNow();
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
        refreshMeta();
        autosaveNow();
    }

    /** Applies a list of moves as one transaction (no animation). */
    public void applyAll(java.util.List<Move> seq) {
        PyraminxState s = state.get();
        for (Move m : seq) s = history.apply(s, m);
        state.set(s);
    }

}
