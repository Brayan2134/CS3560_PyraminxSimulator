package org.example.model.state;

/**
 * CenterPos.java
 * Desc: Canonical index mapping for the 4 three-color center positions (one per face).
 * Invariants:
 *  - Exactly 4 positions in this fixed order: U, L, R, B
 *  - Used as indices into centerAt[4] / centerOri[4] (orientation mod 3).
 */
public enum CenterPos {
    U, L, R, B
}
