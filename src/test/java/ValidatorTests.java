import org.example.model.state.Face;
import org.example.model.PyraminxState;
import org.example.model.moves.TipRotation;
import org.example.model.engine.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidatorTests.java
 * Desc: Negative/positive validator tests using ONLY checkedOf(...) (no unsafeOf).
 * NOTE: Requires PyraminxState.checkedOf(...) to call Validator.requireLegal(...)
 *
 * Invariants verified:
 *  - Edge permutation must be even and a true permutation of 0..5.
 *  - Sum of edge flips is even.
 *  - Centers do NOT permute; center orientations are in {0,1,2}.
 *  - Tip orientations are in {0,1,2}.
 *  - Legal moves preserve legality.
 */
public class ValidatorTests {

    /**
     * Odd edge permutation (swap 0 and 1) should be rejected.
     * Preconditions:
     *  - N/A (constructing via checkedOf)
     * Postconditions:
     *  - IllegalArgumentException is thrown.
     */
    @Test
    void rejectsOddEdgePermutation() {
        assertThrows(IllegalArgumentException.class, () ->
                PyraminxState.checkedOf(
                        new byte[]{0,0,0,0},                 // tipOri
                        new int []{1,0,2,3,4,5},             // edgeAt: 2-cycle → odd parity
                        new byte[]{0,0,0,0,0,0},             // edgeOri
                        new int []{0,1,2,3},                 // centerAt (identity required)
                        new byte[]{0,0,0,0}                  // centerOri
                )
        );
    }

    /**
     * A single flipped edge (odd sum of flips) should be rejected.
     * Postconditions:
     *  - IllegalArgumentException is thrown.
     */
    @Test
    void rejectsSingleFlippedEdge() {
        assertThrows(IllegalArgumentException.class, () ->
                PyraminxState.checkedOf(
                        new byte[]{0,0,0,0},
                        new int []{0,1,2,3,4,5},
                        new byte[]{1,0,0,0,0,0},             // sum = 1 (odd) → illegal
                        new int []{0,1,2,3},
                        new byte[]{0,0,0,0}
                )
        );
    }

    /**
     * Centers must not permute (by design in our model).
     * Postconditions:
     *  - IllegalArgumentException is thrown.
     */
    @Test
    void rejectsCenterSwap() {
        assertThrows(IllegalArgumentException.class, () ->
                PyraminxState.checkedOf(
                        new byte[]{0,0,0,0},
                        new int []{0,1,2,3,4,5},
                        new byte[]{0,0,0,0,0,0},
                        new int []{1,0,2,3},                 // swap U/L → illegal
                        new byte[]{0,0,0,0}
                )
        );
    }

    /**
     * Tip orientation must be in {0,1,2}; out-of-range should be rejected.
     * Postconditions:
     *  - IllegalArgumentException is thrown.
     */
    @Test
    void rejectsOutOfRangeTip() {
        assertThrows(IllegalArgumentException.class, () ->
                PyraminxState.checkedOf(
                        new byte[]{0,0,0,4},                 // invalid tip orientation
                        new int []{0,1,2,3,4,5},
                        new byte[]{0,0,0,0,0,0},
                        new int []{0,1,2,3},
                        new byte[]{0,0,0,0}
                )
        );
    }

    /**
     * Legal moves must preserve legality.
     * Postconditions:
     *  - Validator.requireLegal does NOT throw after applying legal tip moves.
     */
    @Test
    void legalMovesRemainLegal() {
        PyraminxState s = PyraminxState.solved()
                .apply(new TipRotation(Face.U, 1))
                .apply(new TipRotation(Face.L, 2));
        assertDoesNotThrow(() -> Validator.requireLegal(s));
    }

    /**
     * Sanity: solved() is legal.
     * Postconditions:
     *  - Validator.requireLegal does NOT throw.
     */
    @Test
    void solvedIsLegal() {
        assertDoesNotThrow(() -> Validator.requireLegal(PyraminxState.solved()));
    }
}
