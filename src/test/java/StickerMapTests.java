
import org.example.model.state.EdgePos;
import org.example.model.state.Face;
import org.example.view.StickerMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StickerMapTests {
    @Test void otherFacePairs() {
        assertEquals(Face.L, StickerMap.otherFace(EdgePos.UL, Face.U));
        assertEquals(Face.U, StickerMap.otherFace(EdgePos.UL, Face.L));
    }
    @Test void edgeStickerColorRule() {
        assertEquals(Face.U, StickerMap.edgeStickerColorOnFace(Face.U, EdgePos.UR, 0));
        assertEquals(Face.R, StickerMap.edgeStickerColorOnFace(Face.U, EdgePos.UR, 1));
    }
}
