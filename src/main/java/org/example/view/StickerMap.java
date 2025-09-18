package org.example.view;

import org.example.model.PyraminxState;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;

/**
 * StickerMap.java
 * Desc: View-side helpers to translate piece-centric state into face-centric stickers.
 * Role: View adapter (pure functions; no JavaFX types)
 *
 * Conventions:
 *  - Edge orientation 0 means the sticker on 'a' shows color 'a';
 *    orientation 1 swaps colors (sticker on 'a' shows 'b').
 */
public final class StickerMap {
    private StickerMap() {}

    /** The 3 edge positions around a face, in clockwise order as viewed from that face. */
    public static EdgePos[] edgesAround(Face f) {
        return switch (f) {
            case U -> new EdgePos[]{EdgePos.UL, EdgePos.UR, EdgePos.UB};
            case L -> new EdgePos[]{EdgePos.UL, EdgePos.LB, EdgePos.LR};
            case R -> new EdgePos[]{EdgePos.UR, EdgePos.RB, EdgePos.LR};
            case B -> new EdgePos[]{EdgePos.UB, EdgePos.LB, EdgePos.RB};
        };
    }

    /** For a 2-color edge piece, returns the "other" face given one of its faces. */
    public static Face otherFace(EdgePos pos, Face one) {
        return switch (pos) {
            case UL -> (one == Face.U ? Face.L : Face.U);
            case UR -> (one == Face.U ? Face.R : Face.U);
            case UB -> (one == Face.U ? Face.B : Face.U);
            case LR -> (one == Face.L ? Face.R : Face.L);
            case LB -> (one == Face.L ? Face.B : Face.L);
            case RB -> (one == Face.R ? Face.B : Face.R);
        };
    }

    /**
     * Visible color on face 'f' from the edge sitting at position 'pos' with orientation 'ori'.
     * ori==0 → face f shows color f; ori==1 → face f shows the OTHER face's color.
     */
    public static Face edgeStickerColorOnFace(Face f, EdgePos pos, int ori) {
        return (ori & 1) == 0 ? f : otherFace(pos, f);
    }

    public static Face edgeStickerColorOnFace(PyraminxState s, Face f, EdgePos pos) {
        int ori = s.edgeOrientation(pos) & 1;                // 0 or 1
        int pieceId = s.edgeAt(pos);                         // which edge piece is at 'pos'
        EdgePos piece = EdgePos.values()[pieceId];
        return (ori == 0) ? f : otherFaceOfPiece(piece, f);
    }

    public static Face edgeStickerPieceTint(PyraminxState s, Face f, EdgePos pos) {
        int pieceId = s.edgeAt(pos);
        EdgePos piece = EdgePos.values()[pieceId];
        return otherFaceOfPiece(piece, f);
    }

    public static Face otherFaceOfPiece(EdgePos piece, Face one) {
        return switch (piece) {
            case UL -> (one == Face.U ? Face.L : Face.U);
            case UR -> (one == Face.U ? Face.R : Face.U);
            case UB -> (one == Face.U ? Face.B : Face.U);
            case LR -> (one == Face.L ? Face.R : Face.L);
            case LB -> (one == Face.L ? Face.B : Face.L);
            case RB -> (one == Face.R ? Face.B : Face.R);
        };
    }
}
