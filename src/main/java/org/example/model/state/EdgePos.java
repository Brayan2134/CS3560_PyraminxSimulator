package org.example.model.state;

/**
 * EdgePos.java
 * Desc: Canonical index mapping for the 6 edge positions on a Pyraminx.
 * NOTE: Names encode adjacent faces. This order is our global contract.
 * Invariants:
 *  - Exactly 6 positions in this fixed order:
 *    UL, UR, UB, LR, LB, RB
 *  - Used as indices into edgeAt[6] / edgeOri[6].
 */
public enum EdgePos {
    UL, UR, UB, LR, LB, RB
}
