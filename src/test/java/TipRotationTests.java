import org.example.model.state.Face;
import org.example.model.Move;
import org.example.model.PyraminxState;
import org.example.model.moves.TipRotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TipRotationTests.java
 * Desc: Unit tests for tip-only milestone.
 */
public class TipRotationTests {

    @Test void tripleTipIsIdentity() {
        PyraminxState s0 = PyraminxState.solved();
        Move u = new TipRotation(Face.U, 1);
        PyraminxState s = s0.apply(u).apply(u).apply(u);
        Assertions.assertEquals(s0, s, "u u u should be identity");
    }

    @Test void inverseUndoes() {
        PyraminxState s0 = PyraminxState.solved();
        Move l = new TipRotation(Face.L, 1);
        PyraminxState s1 = s0.apply(l);
        PyraminxState s2 = s1.apply(l.inverse());
        Assertions.assertEquals(s0, s2);
    }

    @Test void immutability() {
        PyraminxState s0 = PyraminxState.solved();
        PyraminxState s1 = s0.apply(new TipRotation(Face.R, 2));
        Assertions.assertNotEquals(s0, s1);
        Assertions.assertEquals(0, s0.tipOrientation(Face.R));
        Assertions.assertEquals(2, s1.tipOrientation(Face.R));
        Assertions.assertEquals(0, s1.tipOrientation(Face.U));
    }
}
