import org.example.model.PyraminxState;
import org.example.model.engine.Validator;
import org.example.model.moves.MoveLibrary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserAndScrambleTests {

    @Test
    void parsesBasicTokens() {
        var moves = MoveLibrary.parse("U L' r2 u");
        assertEquals(4, moves.size());

        PyraminxState s = PyraminxState.solved();
        for (var m : moves) s = s.apply(m);

        final PyraminxState sFinal = s; // final snapshot for lambda capture
        assertDoesNotThrow(() -> Validator.requireLegal(sFinal));
    }

    @Test
    void rejectsBadTokens() {
        assertThrows(IllegalArgumentException.class, () -> MoveLibrary.parse("X"));
        assertThrows(IllegalArgumentException.class, () -> MoveLibrary.parse("U3"));
        assertThrows(IllegalArgumentException.class, () -> MoveLibrary.parse("u''"));
    }

    @Test
    void deterministicScrambleWithSeed() {
        var s1 = applyAll(PyraminxState.solved(), MoveLibrary.scramble(10, 42L));
        var s2 = applyAll(PyraminxState.solved(), MoveLibrary.scramble(10, 42L));
        assertEquals(s1, s2); // same seed → same sequence/state
    }

    private static PyraminxState applyAll(PyraminxState s, java.util.List<org.example.model.Move> alg) {
        for (var m : alg) s = s.apply(m);
        return s;
    }
}
