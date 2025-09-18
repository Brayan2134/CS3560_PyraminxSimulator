package org.example.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.example.model.PyraminxState;
import org.example.model.state.CenterPos;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;

/**
 * FxRenderer.java
 * Desc: Minimal diagnostic renderer: shows four face panels with center orientation.
 * Role: View (read-only)
 *
 * Postconditions:
 *  - Repaints whenever the observed state changes.
 *  - Safe to replace later with proper triangular stickers.
 */
public final class FxRenderer extends Pane {
    private final ReadOnlyObjectProperty<PyraminxState> state;

    public FxRenderer(ReadOnlyObjectProperty<PyraminxState> state) {
        this.state = state;
        setPadding(new Insets(12));
        setMinSize(600, 400);
        // initial paint + subscribe
        repaint(state.get());
        state.addListener((obs, oldS, newS) -> repaint(newS));
    }

    /**
     * Repaints the whole canvas from the given state.
     * Role: View
     *
     * Preconditions:
     *  @param s != null
     *
     * Postconditions:
     *  - Clears previous nodes and draws 4 face triangles (U,L,R,B) in a 2×2 grid.
     *  - Edge stickers are colored from the piece @ position with its orientation.
     *  - Tip marker uses the face’s base color; center orientation shown as text.
     */
    private void repaint(PyraminxState s) {
        getChildren().clear();
        Group g = new Group();

        // Triangle layout (equilateral): side length and derived height
        double side = 160;
        double gap  = 18;
        double h    = side * Math.sqrt(3) / 2.0;

        // 2×2 placement of faces
        addFace(g, Face.U, 0,          0,       side, s);
        addFace(g, Face.B, side + gap, 0,       side, s);
        addFace(g, Face.L, 0,          h + gap, side, s);
        addFace(g, Face.R, side + gap, h + gap, side, s);

        getChildren().add(g);
    }

    /**
     * Draws one face triangle at (x,y) with side length 'a', plus tip & three edge stickers.
     * Role: View helper
     *
     * Preconditions:
     *  @param f != null, @param s != null
     *  Coordinates define the top-left “box” containing an upright equilateral triangle.
     *
     * Postconditions:
     *  - Adds JavaFX nodes for the face polygon, three edge sticker circles, a tip marker,
     *    and a small label with center orientation.
     */
    private void addFace(Group g, Face f, double x, double y, double a, PyraminxState s) {
        double h = a * Math.sqrt(3) / 2.0;

        // Triangle vertices:    A(top)   B(bottom-left)   C(bottom-right)
        double ax = x + a/2.0, ay = y;
        double bx = x,         by = y + h;
        double cx = x + a,     cy = y + h;

        // Main face
        Polygon tri = new Polygon(ax, ay, bx, by, cx, cy);
        tri.setFill(Palette.color(f).deriveColor(0, 1, 1, 0.30));
        tri.setStroke(Color.GRAY);
        tri.setStrokeWidth(1.5);

        // Centroid & edge midpoints
        double gx = (ax + bx + cx) / 3.0, gy = (ay + by + cy) / 3.0;
        Point2D mTop  = new Point2D((ax + cx) / 2.0, (ay + cy) / 2.0);
        Point2D mLeft = new Point2D((ax + bx) / 2.0, (ay + by) / 2.0);
        Point2D mRight= new Point2D((bx + cx) / 2.0, (by + cy) / 2.0);

        // Pull midpoints slightly toward center so stickers sit inside the face
        double inset = 0.22;
        Point2D pTop   = interp(mTop,  new Point2D(gx, gy), inset);
        Point2D pLeft  = interp(mLeft, new Point2D(gx, gy), inset);
        Point2D pRight = interp(mRight,new Point2D(gx, gy), inset);
        double r = a * 0.06; // sticker radius

        // Sticker colors via mapping from state → visible face color
        EdgePos[] ring = StickerMap.edgesAround(f);
        // Map ring[0], ring[1], ring[2] to LEFT, RIGHT, TOP edges respectively
        Face cLeft  = StickerMap.edgeStickerColorOnFace(f, ring[0], s.edgeOrientation(ring[0]));
        Face cRight = StickerMap.edgeStickerColorOnFace(f, ring[1], s.edgeOrientation(ring[1]));
        Face cTop   = StickerMap.edgeStickerColorOnFace(f, ring[2], s.edgeOrientation(ring[2]));

        Circle leftSticker  = new Circle(pLeft.getX(),  pLeft.getY(),  r, Palette.color(cLeft));
        Circle rightSticker = new Circle(pRight.getX(), pRight.getY(), r, Palette.color(cRight));
        Circle topSticker   = new Circle(pTop.getX(),   pTop.getY(),   r, Palette.color(cTop));
        leftSticker.setStroke(Color.BLACK.deriveColor(0,1,1,0.35));
        rightSticker.setStroke(Color.BLACK.deriveColor(0,1,1,0.35));
        topSticker.setStroke(Color.BLACK.deriveColor(0,1,1,0.35));

        // Tip marker near vertex A (purely cosmetic)
        Point2D tipPos = interp(new Point2D(ax, ay), new Point2D(gx, gy), 0.18);
        Circle tip = new Circle(tipPos.getX(), tipPos.getY(), r * 0.9, Palette.color(f));
        tip.setStroke(Color.DARKGRAY);

        // Label for center orientation (diagnostic)
        int cOri = s.centerOrientation(CenterPos.values()[f.ordinal()]);
        Label lbl = new Label(f.name() + "  cOri=" + cOri);
        lbl.setLayoutX(x + 8);
        lbl.setLayoutY(y + 8);
        lbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        g.getChildren().addAll(tri, leftSticker, rightSticker, topSticker, tip, lbl);
    }

    private void addPanel(Group g, String name, Color color, double x, double y, int centerOri) {
        Rectangle r = new Rectangle(x, y, 140, 100);
        r.setArcWidth(16); r.setArcHeight(16);
        r.setFill(color); r.setStroke(Color.GRAY);

        Label title = new Label(name);
        title.setLayoutX(x + 8); title.setLayoutY(y + 6);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label ori = new Label("center ori = " + centerOri + " (mod 3)");
        ori.setLayoutX(x + 8); ori.setLayoutY(y + 34);

        g.getChildren().addAll(r, title, ori);
    }

    /**
     * Linear interpolate from p → q by factor t (0..1).
     * Role: tiny math helper
     *
     * Postconditions:
     *  - Returns (1-t)*p + t*q.
     */
    private static Point2D interp(Point2D p, Point2D q, double t) {
        return new Point2D(p.getX() + (q.getX() - p.getX()) * t,
                p.getY() + (q.getY() - p.getY()) * t);
    }
}
