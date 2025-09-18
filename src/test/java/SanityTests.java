import org.example.model.Move;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.example.model.PyraminxState;
import org.example.model.moves.*;
import org.example.model.state.Face;
import java.util.List;

public class SanityTests {

    private PyraminxState applySeq(PyraminxState s, String alg) {
        for (Move m : MoveLibrary.parse(alg)) s = m.apply(s);
        return s;
    }

    @Test void tripleTurnIsIdentity_layer() {
        for (Face f : Face.values()) {
            PyraminxState s = PyraminxState.solved();
            s = applySeq(s, f.name()+" "+f.name()+" "+f.name());
            assertTrue(s.isSolved(), "Triple turn of "+f+" should be identity");
        }
    }

    @Test void tripleTurnIsIdentity_tip() {
        for (Face f : Face.values()) {
            PyraminxState s = PyraminxState.solved();
            String t = f.name().toLowerCase(); // u/l/r/b
            s = applySeq(s, t+" "+t+" "+t);
            assertTrue(s.isSolved(), "Triple tip on "+f+" should be identity");
        }
    }

    @Test void moveThenInverseIsIdentity() {
        PyraminxState s = PyraminxState.solved();
        s = applySeq(s, "U U2");   assertTrue(s.isSolved());
        s = applySeq(s, "L2 L");   assertTrue(s.isSolved());
        s = applySeq(s, "r r2");   assertTrue(s.isSolved());
    }

    @Test void sequenceWithProgrammaticInverseSolves() {
        String seq = "U L R B u l r b U L2 R2";
        List<Move> m = MoveLibrary.parse(seq);
        List<Move> inv = m.stream().map(Move::inverse).toList();
        // apply seq
        PyraminxState s = PyraminxState.solved();
        for (Move x : m) s = x.apply(s);
        // apply inverse in reverse order
        for (int i = inv.size()-1; i >= 0; i--) s = inv.get(i).apply(s);
        assertTrue(s.isSolved());
    }

    @Test void centersAndTipsRulesHold() {
        PyraminxState s = PyraminxState.solved();

        // Tip turn affects only tip
        s = applySeq(s, "u");
        assertEquals(1, s.tipOrientation(Face.U));
        assertEquals(0, s.centerOrientation(org.example.model.state.CenterPos.U));
        // Layer turn bumps U center ori
        s = PyraminxState.solved();
        s = applySeq(s, "U");
        assertEquals(1, s.centerOrientation(org.example.model.state.CenterPos.U));
        assertEquals(0, s.centerOrientation(org.example.model.state.CenterPos.L));
    }

    @Test void scrambleThenUndoAll() {
        PyraminxState s = PyraminxState.solved();
        List<Move> scramble = MoveLibrary.scramble(20);
        for (Move m : scramble) s = m.apply(s);
        // apply inverse
        for (int i = scramble.size()-1; i>=0; i--) s = scramble.get(i).inverse().apply(s);
        assertTrue(s.isSolved());
    }
}
