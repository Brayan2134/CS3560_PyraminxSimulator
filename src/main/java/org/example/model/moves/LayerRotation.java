package org.example.model.moves;

import org.example.model.Move;
import org.example.model.PyraminxState;
import org.example.model.engine.PermutationTables;
import org.example.model.engine.Validator;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;

/**
 * LayerRotation.java
 * Desc: Rotates a full vertex layer (e.g., U) by 120° steps; affects 3 edges and the
 *       center under that tip (orientation only). Tips are NOT changed here.
 * Invariants:
 *  - turns normalized to {0,1,2} where 1 = +120° CW, 2 = +240° (== -120° CCW)
 */
public final class LayerRotation implements Move {
    private final Face face;
    private final int turns;

    /**
     * Creates a layer rotation command.
     * Preconditions:
     *  @param face != null
     *  @param turns any integer; will be reduced mod 3
     * Postconditions:
     *  - Immutable move; no side effects.
     */
    public LayerRotation(Face face, int turns) {
        this.face = face;
        this.turns = Math.floorMod(turns, 3);
    }

    /**
     * Applies the layer rotation to produce a NEW legal state.
     * Preconditions:
     *  @param s != null
     * Postconditions:
     *  - Exactly the U-layer edges (UL, UR, UB) are 3-cycled; their orientations unchanged.
     *  - Center under the turning tip (U) has orientation += turns (mod 3).
     *  - Returns a fresh, validated PyraminxState.
     *
     * @throws UnsupportedOperationException if called for faces not yet implemented
     */
    @Override
    public PyraminxState apply(PyraminxState s) {
        if (turns == 0) return s;

        // 1) Read current arrays out of the immutable state
        byte[] tipOri = new byte[Face.values().length];
        for (Face f : Face.values()) tipOri[f.ordinal()] = s.tipOrientation(f);

        int edgeCount = EdgePos.values().length;
        int[] edgeAt = new int[edgeCount];
        byte[] edgeOri = new byte[edgeCount];
        for (EdgePos p : EdgePos.values()) {
            edgeAt[p.ordinal()] = s.edgeAt(p);
            edgeOri[p.ordinal()] = s.edgeOrientation(p);
        }

        int centerCount = CenterPos.values().length;
        int[] centerAt = new int[centerCount];
        byte[] centerOri = new byte[centerCount];
        for (CenterPos p : CenterPos.values()) {
            centerAt[p.ordinal()] = s.centerAt(p);         // should be identity by design
            centerOri[p.ordinal()] = s.centerOrientation(p);
        }

        // 2) Compute one CW cycle and orientation deltas
        EdgePos[] cyc = PermutationTables.edgeCycleCW(face);
        byte[] dEdgeOri = PermutationTables.edgeOriDelta(face, 1);

        // 3) Apply the cycle 'turns' times (1=CW, 2=CCW via CW twice)
        for (int t = 0; t < turns; t++) {
            // rotate piece IDs at the cycled positions CW
            int tmpId = edgeAt[cyc[cyc.length - 1].ordinal()];
            byte tmpOri = edgeOri[cyc[cyc.length - 1].ordinal()];
            for (int i = cyc.length - 1; i > 0; i--) {
                int to = cyc[i].ordinal();
                int from = cyc[i - 1].ordinal();
                edgeAt[to] = edgeAt[from];
                edgeOri[to] = (byte) ((edgeOri[from] + dEdgeOri[i]) & 1);
            }
            edgeAt[cyc[0].ordinal()] = tmpId;
            edgeOri[cyc[0].ordinal()] = (byte) ((tmpOri + dEdgeOri[0]) & 1);
        }

        // 4) Center orientation under the tip spins by 'turns' (mod 3)
        int cIdx = face.ordinal(); // CenterPos shares U,L,R,B order
        centerOri[cIdx] = (byte) Math.floorMod(
                centerOri[cIdx] + PermutationTables.centerOriDelta(face, turns), 3);


        // 5) Build a NEW validated state and return
        PyraminxState next = PyraminxState.checkedOf(tipOri, edgeAt, edgeOri, centerAt, centerOri);
        // Optional assert during dev:
        Validator.requireLegal(next);
        return next;
    }

    /**
     * Returns the inverse (3 - turns) % 3.
     * Postconditions:
     *  - Compose with this move to yield identity on any legal state.
     */
    @Override
    public Move inverse() {
        return new LayerRotation(face, (3 - turns) % 3);
    }

    /**
     * WCA-style notation: "U", "U2" (CCW is 2 since 3-step group).
     * Postconditions:
     *  - "" for no-op (0 turns), else uppercase face + optional "2".
     */
    @Override
    public String notation() {
        return switch (turns) {
            case 0 -> "";
            case 1 -> face.name();       // "U"
            default -> face.name() + "2"; // "U2" == CCW 120°
        };
    }

    /** Accessors (helpers for tests/UI). */
    public Face face() { return face; }
    public int turns() { return turns; }
}
