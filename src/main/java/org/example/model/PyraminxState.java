package org.example.model;

import org.example.model.engine.Validator;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;

import java.util.Arrays;

/**
 * PyraminxState.java
 * Desc: Immutable snapshot of the puzzle. Now includes tips (mod-3), 6 edges, 4 centers.
 * NOTE: This file only establishes representation; layer moves come next.
 * Invariants:
 *  - tipOri.length == 4,   values in {0,1,2}
 *  - edgeAt.length == 6,   edgeOri.length == 6, edgeOri in {0,1}
 *  - centerAt.length == 4, centerOri.length == 4, centerOri in {0,1,2}
 *  - edgeAt is a permutation of 0..5; centerAt is a permutation of 0..3
 *  - This class is immutable; no method mutates internal arrays.
 */
public final class PyraminxState {

    // Tips: index by Face.ordinal() → 0..3
    private final byte[] tipOri;

    // Edges: piece-centric arrays
    // edgeAt[pos] = which edge-ID currently sits at 'pos' (0..5). Identity at solved().
    private final int[] edgeAt;
    // edgeOri[pos] = orientation bit for the edge sitting at 'pos' (0 or 1).
    private final byte[] edgeOri;

    // Centers: 3-color
    // centerAt[pos] = which center-ID (0..3) sits at 'pos'. Identity at solved().
    private final int[] centerAt;
    // centerOri[pos] = orientation mod 3 for the center sitting at 'pos'.
    private final byte[] centerOri;

    /**
     * Private ctor used by factory methods. Assumes inputs already validated/normalized.
     * Preconditions:
     *
     * @param tipOri length   == 4
     * @param edgeAt length   == 6 && edgeOri.length == 6
     * @param centerAt length == 4 && centerOri.length == 4
     *                        Postconditions:
     *                        - Defensive copies are stored; object is immutable thereafter.
     */
    private PyraminxState(byte[] tipOri, int[] edgeAt, byte[] edgeOri, int[] centerAt, byte[] centerOri) {
        this.tipOri = tipOri.clone();
        this.edgeAt = edgeAt.clone();
        this.edgeOri = edgeOri.clone();
        this.centerAt = centerAt.clone();
        this.centerOri = centerOri.clone();
    }

    /**
     * Creates the canonical solved state.
     * Postconditions:
     * - tipOri = {0,0,0,0}
     * - edgeAt = {0,1,2,3,4,5}, edgeOri = all 0
     * - centerAt = {0,1,2,3}, centerOri = all 0
     */
    public static PyraminxState solved() {
        return new PyraminxState(
                new byte[]{0, 0, 0, 0},
                new int[]{0, 1, 2, 3, 4, 5},
                new byte[]{0, 0, 0, 0, 0, 0},
                new int[]{0, 1, 2, 3},
                new byte[]{0, 0, 0, 0}
        );
    }

    /**
     * Read the orientation (0..2) for a tip.
     * Preconditions:
     *
     * @param f != null
     *          Postconditions:
     *          - Returns a value in {0,1,2}.
     */
    public byte tipOrientation(Face f) {
        return tipOri[f.ordinal()];
    }

    /**
     * Returns a NEW state with the given tip set to newOri (wrapped mod 3).
     * Role: Core functional update for tips; edges/centers unchanged.
     */
    public PyraminxState withTipOrientation(Face f, int newOri) {
        byte[] t = tipOri.clone();
        t[f.ordinal()] = (byte) Math.floorMod(newOri, 3);
        return new PyraminxState(t, edgeAt, edgeOri, centerAt, centerOri);
    }

    /**
     * Returns true iff this state is in the canonical solved position/orientation.
     * Why: lets the view decide whether to show physical mono-color or piece-tint.
     */
    public boolean isSolved() {
        // tips all 0, centers at identity with ori 0 (mod 3), edges at identity with ori 0
        for (int i = 0; i < tipOri.length; i++) if (tipOri[i] != 0) return false;
        for (int i = 0; i < centerAt.length; i++) if (centerAt[i] != i || centerOri[i] != 0) return false;
        for (int i = 0; i < edgeAt.length; i++) if (edgeAt[i] != i || edgeOri[i] != 0) return false;
        return true;
    }


    /*--------------------------------------- helper functions ---------------------------------------*/

    /**
     * Edge piece ID at a position. Role: Core getter.
     */
    public int edgeAt(EdgePos pos) {
        return edgeAt[pos.ordinal()];
    }


    /**
     * Edge orientation bit (0 or 1) at a position. Role: Core getter.
     */
    public byte edgeOrientation(EdgePos pos) {
        return edgeOri[pos.ordinal()];
    }

    /**
     * Center piece ID at a position. Role: Core getter.
     */
    public int centerAt(CenterPos pos) {
        return centerAt[pos.ordinal()];
    }

    /**
     * Center orientation (0..2) at a position. Role: Core getter.
     */
    public byte centerOrientation(CenterPos pos) {
        return centerOri[pos.ordinal()];
    }

    /**
     * Functional application of a move (delegates to Move).
     */
    public PyraminxState apply(Move m) {
        return m.apply(this);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof PyraminxState other)) return false;
        return Arrays.equals(tipOri, other.tipOri) &&
                Arrays.equals(edgeAt, other.edgeAt) &&
                Arrays.equals(edgeOri, other.edgeOri) &&
                Arrays.equals(centerAt, other.centerAt) &&
                Arrays.equals(centerOri, other.centerOri);
    }
    @Override public int hashCode() {
        int h = Arrays.hashCode(tipOri);
        h = 31*h + Arrays.hashCode(edgeAt);
        h = 31*h + Arrays.hashCode(edgeOri);
        h = 31*h + Arrays.hashCode(centerAt);
        h = 31*h + Arrays.hashCode(centerOri);
        return h;
    }
    @Override public String toString() {
        return "Tips=" + Arrays.toString(tipOri) +
                " Edges@=" + Arrays.toString(edgeAt) +
                " eOri=" + Arrays.toString(edgeOri) +
                " Centers@=" + Arrays.toString(centerAt) +
                " cOri=" + Arrays.toString(centerOri);
    }

    /*--------------------------------------- negative tests ---------------------------------------*/

    /**
     * Creates a state and enforces legality immediately.
     * Role: Core (checked factory)
     *
     * Preconditions:
     *  - Arrays have correct lengths; values must represent a legal state.
     *
     * Postconditions:
     *  - Returns a new, validated PyraminxState.
     *  - Throws IllegalArgumentException if invariants are violated.
     */
    public static PyraminxState checkedOf(byte[] tipOri, int[] edgeAt, byte[] edgeOri,
                                          int[] centerAt, byte[] centerOri) {
        PyraminxState s = new PyraminxState(tipOri, edgeAt, edgeOri, centerAt, centerOri);
        Validator.requireLegal(s);
        return s;
    }


}