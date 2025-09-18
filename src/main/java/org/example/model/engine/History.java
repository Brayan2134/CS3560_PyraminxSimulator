package org.example.model.engine;

import org.example.model.Move;
import org.example.model.PyraminxState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * History.java
 * Desc: Command-history stack for undo/redo of model moves.
 * Role: Core (model utility)
 *
 * Invariants:
 *  - Stores moves (not states); undo applies inverse() of the last move.
 *  - apply(...) pushes onto undo and clears redo.
 */
public final class History {
    private final Deque<Move> undo = new ArrayDeque<>();
    private final Deque<Move> redo = new ArrayDeque<>();

    /**
     * Applies a move to the given state, pushes it onto undo, clears redo, returns NEW state.
     * Preconditions:
     *  @param s != null, @param m != null
     * Postconditions:
     *  - Returns s' = m.apply(s); redo is cleared; undo size increments by 1.
     */
    public PyraminxState apply(PyraminxState s, Move m) {
        PyraminxState next = m.apply(s);
        undo.push(m);
        redo.clear();
        return next;
    }

    /**
     * Undoes the last move (if any) by applying its inverse.
     * Postconditions:
     *  - If undo empty: returns s unchanged.
     *  - Else: returns s' = inverse(undo.pop()).apply(s); pushed move to redo.
     */
    public PyraminxState undo(PyraminxState s) {
        if (undo.isEmpty()) return s;
        Move m = undo.pop();
        PyraminxState prev = m.inverse().apply(s);
        redo.push(m);
        return prev;
    }

    /**
     * Redoes the last undone move (if any).
     * Postconditions:
     *  - If redo empty: returns s unchanged.
     *  - Else: returns s' = redoMove.apply(s); pushes redoMove back to undo.
     */
    public PyraminxState redo(PyraminxState s) {
        if (redo.isEmpty()) return s;
        Move m = redo.pop();
        PyraminxState next = m.apply(s);
        undo.push(m);
        return next;
    }

    /** Helpers for UI/debug. */
    public int undoSize() { return undo.size(); }
    public int redoSize() { return redo.size(); }
    public void clear() { undo.clear(); redo.clear(); }

    /**
     * Renders the undo stack as chronological notation (oldest → newest).
     * Why: Useful for UI display / copy.
     * Postconditions: string like "U L r2 u" or "" if empty.
     */
    public String toAlg() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Move> it = undo.descendingIterator(); it.hasNext(); ) {
            sb.append(it.next().notation());
            if (it.hasNext()) sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Renders the inverse of the undo stack (useful as a solving sequence).
     * Postconditions: inverse applied newest → oldest, e.g., "u r2 L' U2".
     */
    public String toInverseAlg() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Move> it = undo.iterator(); it.hasNext(); ) {
            sb.append(it.next().inverse().notation());
            if (it.hasNext()) sb.append(' ');
        }
        return sb.toString();
    }
}
