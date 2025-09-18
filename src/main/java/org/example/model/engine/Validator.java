package org.example.model.engine;

import org.example.model.state.Face;
import org.example.model.PyraminxState;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;

/**
 * Validator.java
 * Desc: Centralized legality checks for PyraminxState invariants.
 * Role: Core (model correctness gate)
 *
 * Invariants enforced:
 *  - tipOri ∈ {0,1,2}
 *  - edgeAt is a permutation of 0..5
 *  - edgeOri ∈ {0,1} and sum(edgeOri) is even
 *  - centers do not permute; centerOri ∈ {0,1,2}
 *  - edge permutation parity is even
 */
public final class Validator {
    private Validator() {}

    /**
     * Validates that a state satisfies all model invariants.
     * Role: Core
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Returns normally if the state is legal.
     *  - Throws IllegalArgumentException with a descriptive message otherwise.
     */
    public static void requireLegal(PyraminxState s) {
        // Ranges: tips
        for (var f : Face.values()) {
            int t = s.tipOrientation(f);
            if (t < 0 || t > 2) throw iae("Tip orientation out of range: " + f + "=" + t);
        }

        // Ranges: edges
        for (var p : EdgePos.values()) {
            int id = s.edgeAt(p);
            int o  = s.edgeOrientation(p);
            int n  = EdgePos.values().length;
            if (id < 0 || id >= n)                    throw iae("Edge id out of range at " + p + ": " + id);
            if (o != 0 && o != 1)                     throw iae("Edge ori out of range at " + p + ": " + o);
        }

        // Ranges + no permutation: centers must stay in place (positions are fixed by definition)
        for (var p : CenterPos.values()) {
            int id = s.centerAt(p);
            int o  = s.centerOrientation(p);
            if (id != p.ordinal())                    throw iae("Centers must not permute: " + p + " has id " + id);
            if (o < 0 || o > 2)                       throw iae("Center ori out of range at " + p + ": " + o);
        }

        // Structural: edgeAt must be a permutation of 0..5
        if (!isPermutation(s))                       throw iae("edgeAt is not a permutation of 0..5");

        // Parity invariants
        if (!isEvenPermutation(s))                   throw iae("Edge permutation has odd parity");
        if (!edgeFlipSumEven(s))                     throw iae("Sum of edge flips must be even");
    }

    /**
     * Checks that edgeAt is a permutation of 0..5 (no duplicates/missing).
     * Role: Helper (used internally by requireLegal)
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Returns true iff each edge ID appears exactly once.
     */
    private static boolean isPermutation(PyraminxState s) {
        int n = EdgePos.values().length;
        boolean[] seen = new boolean[n];
        for (var p : EdgePos.values()) {
            int id = s.edgeAt(p);
            if (id < 0 || id >= n || seen[id]) return false;
            seen[id] = true;
        }
        return true;
    }

    /**
     * Computes edge permutation parity using inversion count (even = legal).
     * Role: Helper (used internally by requireLegal)
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Returns true iff permutation has even parity.
     */
    private static boolean isEvenPermutation(PyraminxState s) {
        int n = EdgePos.values().length;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = s.edgeAt(EdgePos.values()[i]);
        long inv = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (a[i] > a[j]) inv++;
            }
        }
        return (inv % 2) == 0;
    }

    /**
     * Checks that the total number of flipped edges is even.
     * Role: Helper (used internally by requireLegal)
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Returns true iff sum(edgeOri) % 2 == 0.
     */
    private static boolean edgeFlipSumEven(PyraminxState s) {
        int sum = 0;
        for (var p : EdgePos.values()) sum += s.edgeOrientation(p);
        return (sum & 1) == 0;
    }

    /**
     * Constructs an IllegalArgumentException with a standard format.
     * Role: Helper (error utility)
     *
     * Preconditions:
     *  @param msg != null
     *
     * Postconditions:
     *  - Returns a new IllegalArgumentException with the given message.
     */
    private static IllegalArgumentException iae(String msg) {
        return new IllegalArgumentException(msg);
    }
}
