package org.example.model;

/**
 * Move.java
 * Desc: Command for transforming a PyraminxState. Enables polymorphic apply/undo.
 * Invariants:
 *  - Implementations must be side-effect free (functional).
 */
public interface Move {

    /**
     * Applies this move to the provided state and returns a NEW state.
     * Preconditions:
     *  @param s != null
     * Postconditions:
     *  - Returns a new PyraminxState (original is unchanged).
     */
    PyraminxState apply(PyraminxState s);

    /**
     * Returns the inverse move such that apply(m).apply(m.inverse()) == identity.
     * Postconditions:
     *  - m.apply(s).apply(m.inverse()) equals s for all legal s.
     */
    Move inverse();

    /** Human-readable WCA-style notation (e.g., "u", "u2"). */
    String notation();
}
