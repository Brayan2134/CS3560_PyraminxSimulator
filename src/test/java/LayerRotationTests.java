import org.example.model.PyraminxState;
import org.example.model.engine.Validator;
import org.example.model.moves.LayerRotation;
import org.example.model.moves.TipRotation;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LayerRotationTests.java
 * Desc: Unit tests for the first real layer move (U).
 */
public class LayerRotationTests {

    /**
     * U * U * U = identity.
     */
    @Test
    void tripleUIsIdentity() {
        PyraminxState s0 = PyraminxState.solved();
        var U = new LayerRotation(Face.U, 1);
        PyraminxState s = s0.apply(U).apply(U).apply(U);
        assertEquals(s0, s);
        assertDoesNotThrow(() -> Validator.requireLegal(s));
    }

    /**
     * U then U' (our inverse is "U2" because 3-step group) is identity.
     */
    @Test
    void uUndoesUPrime() {
        PyraminxState s0 = PyraminxState.solved();
        var U = new LayerRotation(Face.U, 1);
        PyraminxState s = s0.apply(U).apply(U.inverse());
        assertEquals(s0, s);
    }

    /**
     * Only UL, UR, UB edges should move; LR, LB, RB must remain unchanged.
     */
    @Test
    void unaffectedEdgesStayPut() {
        PyraminxState s0 = PyraminxState.solved();
        PyraminxState s1 = s0.apply(new LayerRotation(Face.U, 1));

        // check the three non-U edges
        assertEquals(EdgePos.LR.ordinal(), s1.edgeAt(EdgePos.LR));
        assertEquals(0, s1.edgeOrientation(EdgePos.LR));

        assertEquals(EdgePos.LB.ordinal(), s1.edgeAt(EdgePos.LB));
        assertEquals(0, s1.edgeOrientation(EdgePos.LB));

        assertEquals(EdgePos.RB.ordinal(), s1.edgeAt(EdgePos.RB));
        assertEquals(0, s1.edgeOrientation(EdgePos.RB));
    }

    /**
     * Center under U tip gains +turns orientation; others unchanged.
     */
    @Test
    void centerOrientationUpdatesOnlyAtU() {
        PyraminxState s0 = PyraminxState.solved();
        PyraminxState s1 = s0.apply(new LayerRotation(Face.U, 1));

        assertEquals(1, s1.centerOrientation(CenterPos.U));
        assertEquals(0, s1.centerOrientation(CenterPos.L));
        assertEquals(0, s1.centerOrientation(CenterPos.R));
        assertEquals(0, s1.centerOrientation(CenterPos.B));

        // 2 more U turns should bring it back to 0 (mod 3)
        PyraminxState s3 = s1.apply(new LayerRotation(Face.U, 1))
                .apply(new LayerRotation(Face.U, 1));
        assertEquals(0, s3.centerOrientation(CenterPos.U));
    }

    /**
     * Legal moves keep the state legal (parity/flip-sum preserved).
     */
    @Test
    void validatorStillPassesAfterMoves() {
        PyraminxState s = PyraminxState.solved()
                .apply(new TipRotation(Face.U, 1))
                .apply(new LayerRotation(Face.U, 1))
                .apply(new TipRotation(Face.L, 2));
        assertDoesNotThrow(() -> Validator.requireLegal(s));
    }
}