package org.example.model.engine;

import org.example.model.state.EdgePos;
import org.example.model.state.Face;

/**
 * PermutationTables.java
 * Desc: Minimal precomputed cycles/orientation deltas for layer (vertex) turns.
 * NOTE: Milestone scope = U only. Add L/R/B later with the same pattern.
 * Invariants:
 *  - Edge cycles list the three edge POSITIONS that move for a given face turn.
 *  - Edge orientation deltas are applied to the piece now sitting at that position.
 *  - Centers never permute in this project; only orientation may change.
 */
public final class PermutationTables {
    private PermutationTables() {}

    /**
     * Returns the CW edge cycle for a 120° turn around face f.
     * Preconditions:
     *  @param f != null
     * Postconditions:
     *  - U: {UL, UR, UB}
     *  - L: {UL, LB, LR}
     *  - R: {UR, RB, LR}
     *  - B: {UB, LB, RB}
     */
    public static EdgePos[] edgeCycleCW(Face f) {
        return switch (f) {
            case U -> new EdgePos[]{EdgePos.UL, EdgePos.UR, EdgePos.UB};
            case L -> new EdgePos[]{EdgePos.UL, EdgePos.LB, EdgePos.LR};
            case R -> new EdgePos[]{EdgePos.UR, EdgePos.RB, EdgePos.LR};
            case B -> new EdgePos[]{EdgePos.UB, EdgePos.LB, EdgePos.RB};
        };
    }

    /**
     * Edge orientation deltas for positions in edgeCycleCW(f).
     * Role: Helper
     *
     * Postconditions:
     *  - Returns {0,0,0}. With the chosen indexing, vertex turns do not flip edges.
     *    (If you later change indexing, adjust 1s here to flip where needed.)
     */
    public static byte[] edgeOriDelta(Face f, int turns) {
        return new byte[]{0, 0, 0};
    }

    /**
     * Center orientation delta (mod 3) for a face turn.
     * Role: Helper
     *
     * Postconditions:
     *  - Returns +turns (mod 3) for the turning face.
     */
    public static int centerOriDelta(Face f, int turns) {
        return Math.floorMod(turns, 3);
    }
}
