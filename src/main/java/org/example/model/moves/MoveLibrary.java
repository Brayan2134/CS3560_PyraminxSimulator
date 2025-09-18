package org.example.model.moves;

import org.example.model.Move;
import org.example.model.state.Face;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MoveLibrary.java
 * Desc: Parses simple notation and generates scrambles.
 * Supported tokens: U L R B (layers), u l r b (tips), optionally with ' or 2 (e.g., U', r2).
 * turns mapping: 1 = CW 120°, 2 = CCW 120° (≡ +240°).
 */
public final class MoveLibrary {
    private MoveLibrary() {}

    /**
     * Parses a whitespace-separated algorithm string into moves.
     * Preconditions:
     *  @param alg non-null (may be empty)
     * Postconditions:
     *  - Returns list of Move; throws IllegalArgumentException on bad token.
     */
    public static List<Move> parse(String alg) {
        List<Move> out = new ArrayList<>();
        if (alg == null || alg.isBlank()) return out;
        for (String tok : alg.trim().split("\\s+")) {
            out.add(parseToken(tok));
        }
        return out;
    }

    private static Move parseToken(String tok) {
        if (tok == null || tok.isBlank()) throw iae("Empty token");
        char c = tok.charAt(0);
        boolean isLayer = Character.isUpperCase(c);
        boolean isTip   = Character.isLowerCase(c);
        int turns = 1;
        if (tok.length() > 1) {
            char suffix = tok.charAt(1);
            if (suffix == '\'') turns = 2;
            else if (suffix == '2') turns = 2;
            else throw iae("Bad suffix: " + tok);
            if (tok.length() > 2) throw iae("Extra chars: " + tok);
        }
        Face face = switch (Character.toUpperCase(c)) {
            case 'U' -> Face.U;
            case 'L' -> Face.L;
            case 'R' -> Face.R;
            case 'B' -> Face.B;
            default  -> throw iae("Unknown face: " + tok);
        };
        if (isLayer) return new LayerRotation(face, turns);
        if (isTip)   return new TipRotation(face, turns);
        throw iae("Bad token: " + tok);
    }

    /**
     * Produces a simple random scramble of length n.
     * Constraints:
     *  - Avoid two consecutive moves on the same face & same type (layer vs tip).
     */
    public static List<Move> scramble(int n, long seed) {
        Random rnd = new Random(seed);
        List<Move> out = new ArrayList<>(n);
        Move prev = null;
        for (int i = 0; i < n; i++) {
            Move m;
            do { m = randomMove(rnd); }
            while (prev != null && sameAxisType(prev, m));
            out.add(m);
            prev = m;
        }
        return out;
    }

    public static List<Move> scramble(int n) { return scramble(n, System.nanoTime()); }

    private static Move randomMove(Random rnd) {
        Face face = Face.values()[rnd.nextInt(4)];
        boolean layer = rnd.nextBoolean();           // 50/50 layer vs tip
        int turns = (rnd.nextBoolean() ? 1 : 2);     // 1 or 2
        return layer ? new LayerRotation(face, turns) : new TipRotation(face, turns);
    }

    private static boolean sameAxisType(Move a, Move b) {
        boolean aLayer = a instanceof LayerRotation;
        boolean bLayer = b instanceof LayerRotation;
        Face fa = faceOf(a), fb = faceOf(b);
        return aLayer == bLayer && fa == fb;
    }

    // Extract face for comparison (both moves expose face() accessors)
    private static Face faceOf(Move m) {
        if (m instanceof LayerRotation lr) return lr.face();
        if (m instanceof TipRotation tr)   return tr.face();
        throw iae("Unknown move subtype: " + m.getClass());
    }

    private static IllegalArgumentException iae(String msg) { return new IllegalArgumentException(msg); }
}