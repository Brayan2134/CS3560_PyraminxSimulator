import org.example.model.state.Face;
import org.example.model.PyraminxState;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PyraminxStateBasicsTests.java
 * Desc: Verifies identity permutations/orientations in solved() and immutability on tip updates.
 */
public class PyraminxStateBasicsTests {

    @Test void solvedIsIdentity() {
        PyraminxState s = PyraminxState.solved();

        // tips
        assertEquals(0, s.tipOrientation(Face.U));
        assertEquals(0, s.tipOrientation(Face.L));
        assertEquals(0, s.tipOrientation(Face.R));
        assertEquals(0, s.tipOrientation(Face.B));

        // edges: edgeAt[pos] == pos, edgeOri == 0
        for (EdgePos p : EdgePos.values()) {
            assertEquals(p.ordinal(), s.edgeAt(p));
            assertEquals(0, s.edgeOrientation(p));
        }

        // centers: centerAt[pos] == pos, centerOri == 0
        for (CenterPos p : CenterPos.values()) {
            assertEquals(p.ordinal(), s.centerAt(p));
            assertEquals(0, s.centerOrientation(p));
        }
    }

    @Test void tipWitherIsImmutableAndLocal() {
        PyraminxState s0 = PyraminxState.solved();
        PyraminxState s1 = s0.withTipOrientation(Face.U, 2);

        assertNotEquals(s0, s1);
        // original unchanged
        assertEquals(0, s0.tipOrientation(Face.U));
        // new state changed only U tip
        assertEquals(2, s1.tipOrientation(Face.U));

        // Edges/centers untouched
        for (EdgePos p : EdgePos.values()) {
            assertEquals(p.ordinal(), s1.edgeAt(p));
            assertEquals(0, s1.edgeOrientation(p));
        }
        for (CenterPos p : CenterPos.values()) {
            assertEquals(p.ordinal(), s1.centerAt(p));
            assertEquals(0, s1.centerOrientation(p));
        }
    }
}
