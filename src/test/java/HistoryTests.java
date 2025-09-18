import org.example.model.PyraminxState;
import org.example.model.engine.History;
import org.example.model.engine.Validator;
import org.example.model.moves.LayerRotation;
import org.example.model.moves.TipRotation;
import org.example.model.state.Face;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryTests {

    @Test
    void undoRedoRoundTrip() {
        PyraminxState s = PyraminxState.solved();
        History h = new History();

        s = h.apply(s, new TipRotation(Face.U, 1));
        s = h.apply(s, new LayerRotation(Face.L, 1));
        s = h.apply(s, new TipRotation(Face.R, 2));
        assertEquals(3, h.undoSize());
        assertEquals(0, h.redoSize());

        s = h.undo(s);
        s = h.undo(s);
        s = h.undo(s);
        assertEquals(PyraminxState.solved(), s);
        assertEquals(0, h.undoSize());
        assertEquals(3, h.redoSize());

        s = h.redo(s);
        s = h.redo(s);
        s = h.redo(s);

        final PyraminxState sFinal = s; // final snapshot for lambda capture
        assertDoesNotThrow(() -> Validator.requireLegal(sFinal));
    }

    @Test
    void undoOnEmptyDoesNothing() {
        History h = new History();
        PyraminxState s0 = PyraminxState.solved();
        PyraminxState s1 = h.undo(s0);
        assertSame(s0, s1); // no-op path returns same ref
    }
}
