package org.example.model.moves;

import org.example.model.state.Face;
import org.example.model.Move;
import org.example.model.PyraminxState;

/**
 * TipRotation.java
 * Desc: Rotates a single tip by 120° steps (mod 3).
 * Invariants:
 *  - turns in {0,1,2} after normalization
 */
public final class TipRotation implements Move {
    private final Face face;
    private final int turns; // 1 => +120°, 2 => +240° (equiv to -120°)

    /**
     * Creates a tip rotation.
     * Preconditions:
     *  @param face != null
     *  @param turns any integer; normalized mod 3
     * Postconditions:
     *  - New immutable move instance.
     */
    public TipRotation(Face face, int turns) {
        this.face = face;
        this.turns = Math.floorMod(turns, 3);
    }

    /**
     * Applies this tip rotation to the given PyraminxState.
     * Role: Core
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Returns a NEW PyraminxState (input is unchanged).
     *  - If turns == 0, returns the same state.
     *  - Otherwise, advances the orientation of the selected face's tip by 'turns' (mod 3).
     */
    @Override public PyraminxState apply(PyraminxState s) {
        if (turns == 0) return s;
        int cur = s.tipOrientation(face);
        return s.withTipOrientation(face, cur + turns);
    }

    /**
     * Returns the inverse of this tip rotation.
     * Role: Core
     *
     * Postconditions:
     *  - Applying this move followed by its inverse yields the original state.
     *  - Normalized to {0,1,2}, where (3 - turns) % 3 undoes the rotation.
     */
    @Override public Move inverse() { return new TipRotation(face, (3 - turns) % 3); }

    /**
     * Returns human-readable notation for this tip rotation.
     * Role: Helper (UI/logging aid)
     *
     * Postconditions:
     *  - "" if turns == 0 (no-op).
     *  - Lowercase face letter (e.g., "u") if turns == 1.
     *  - Lowercase face letter + "2" (e.g., "u2") if turns == 2.
     */
    @Override public String notation() {
        return switch (turns) {
            case 0 -> "";
            case 1 -> face.name().toLowerCase();    // "u"
            default -> face.name().toLowerCase()+"2"; // "u2"
        };
    }

    /*--------------------------------------- helper functions ---------------------------------------*/


    /**
     * Accessor for which face this move targets.
     * Role: Helper (introspection/testing)
     *
     * Postconditions:
     *  - Returns a non-null Face; no side effects.
     */
    public Face face() { return face; }

    /**
     * Accessor for the normalized turn count (0,1,2).
     * Role: Helper (introspection/testing)
     *
     * Postconditions:
     *  - Returns the number of 120° steps encoded in this move.
     */
    public int turns() { return turns; }
}
