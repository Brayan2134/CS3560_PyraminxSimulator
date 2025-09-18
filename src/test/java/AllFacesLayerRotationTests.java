import org.example.model.PyraminxState;
import org.example.model.engine.Validator;
import org.example.model.moves.LayerRotation;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AllFacesLayerRotationTests.java
 * Desc: Verifies group properties and locality for U/L/R/B layer turns.
 */
public class AllFacesLayerRotationTests {

    @Test
    void tripleTurnIsIdentityForAllFaces() {
        for (Face f : Face.values()) {
            var m = new LayerRotation(f, 1);
            PyraminxState s0 = PyraminxState.solved();
            PyraminxState s  = s0.apply(m).apply(m).apply(m);
            assertEquals(s0, s, "Triple " + f + " should be identity");
            assertDoesNotThrow(() -> Validator.requireLegal(s));
        }
    }

    @Test
    void moveThenInverseIsIdentityForAllFaces() {
        for (Face f : Face.values()) {
            var m = new LayerRotation(f, 1);
            PyraminxState s0 = PyraminxState.solved();
            PyraminxState s  = s0.apply(m).apply(m.inverse());
            assertEquals(s0, s, f + " then inverse should be identity");
        }
    }

    @Test
    void unaffectedEdgesRemainUnchanged() {
        for (Face f : Face.values()) {
            PyraminxState s0 = PyraminxState.solved();
            PyraminxState s1 = s0.apply(new LayerRotation(f, 1));

            for (EdgePos p : unaffectedEdges(f)) {
                assertEquals(p.ordinal(), s1.edgeAt(p), f + ": edge " + p + " moved unexpectedly");
                assertEquals(0, s1.edgeOrientation(p), f + ": edge ori changed unexpectedly");
            }
        }
    }

    private static EdgePos[] unaffectedEdges(Face f) {
        return switch (f) {
            case U -> new EdgePos[]{EdgePos.LR, EdgePos.LB, EdgePos.RB};
            case L -> new EdgePos[]{EdgePos.UR, EdgePos.UB, EdgePos.RB};
            case R -> new EdgePos[]{EdgePos.UL, EdgePos.UB, EdgePos.LB};
            case B -> new EdgePos[]{EdgePos.UL, EdgePos.UR, EdgePos.LR};
        };
    }
}
