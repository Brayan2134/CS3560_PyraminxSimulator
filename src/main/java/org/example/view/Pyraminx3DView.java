package org.example.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.example.model.PyraminxState;
import org.example.model.moves.LayerRotation;
import org.example.model.moves.TipRotation;
import org.example.model.Move;
import org.example.model.state.EdgePos;
import org.example.model.state.Face;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Pyraminx3DView.java
 * Desc: A 3D JavaFX view (SubScene + PerspectiveCamera) that renders the Pyraminx as a pyramid.
 *       Drag to orbit, scroll to zoom. Buttons animate layer or tip rotations; after the
 *       animation finishes, the provided moveSink is called to mutate the model, then colors refresh.
 *
 * Invariants:
 *  - One MeshView per "sticker" (3 per face for edges + 1 center + 1 tip) grouped under a face Group.
 *  - Faces (U,L,R,B) live under root3D; layer groups (u,l,r,b) are separate so we can rotate them in place.
 *  - Colors are pulled from the current model state on every repaint.
 */
public final class Pyraminx3DView extends BorderPane {

    /** Logical groups so we can rotate a face’s layer/tip cleanly. */
    private final Group root3D = new Group();
    private final Map<Face, Group> faceGroup = new EnumMap<>(Face.class);   // geometry per face
    private final Map<Face, Group> layerGroup = new EnumMap<>(Face.class);  // handles rotation for layer
    private final Map<Face, Group> tipGroup = new EnumMap<>(Face.class);    // handles rotation for tip

    private final SubScene subScene;
    private final PerspectiveCamera cam = new PerspectiveCamera(true);

    private final ReadOnlyObjectProperty<PyraminxState> state;
    private final Consumer<Move> moveSink;

    // Camera orbit parameters
    private double yaw = 35, pitch = -15, distance = 520;
    private double lastX, lastY;

    /**
     * Creates the 3D view and installs orbit controls + UI.
     * Preconditions:
     *  @param state != null  (read-only property of the model state)
     *  @param moveSink may be null; when non-null it's called after an animation to commit the move.
     *
     * Postconditions:
     *  - SubScene with camera is created and embedded.
     *  - Buttons allow rotating a sample face (U) layer or tip; add more as desired.
     *  - Slider snaps yaw to show U/L/R/B quickly.
     */
    public Pyraminx3DView(ReadOnlyObjectProperty<PyraminxState> state, Consumer<Move> moveSink) {
        this.state = state;
        this.moveSink = moveSink;

        buildGeometry();      // once
        setupCamera();        // once
        subScene = new SubScene(root3D, 900, 620, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#f5f6f8"));
        subScene.setCamera(cam);
        installOrbitControls();

        // toolbar (minimal demo: U layer & tip—copy pattern for L/R/B)
        Button u = new Button("U");
        Button u2 = new Button("U2");
        Button ut = new Button("u");    // tip
        Button ut2 = new Button("u2");
        u.setOnAction(e -> animateMove(new LayerRotation(Face.U, 1)));
        u2.setOnAction(e -> animateMove(new LayerRotation(Face.U, 2)));
        ut.setOnAction(e -> animateMove(new TipRotation(Face.U, 1)));
        ut2.setOnAction(e -> animateMove(new TipRotation(Face.U, 2)));

        // "Face picker" slider: 0=U,1=L,2=R,3=B (snaps camera yaw)
        Slider faceSlider = new Slider(0, 3, 0);
        faceSlider.setMajorTickUnit(1);
        faceSlider.setMinorTickCount(0);
        faceSlider.setSnapToTicks(true);
        faceSlider.valueProperty().addListener((o, a, v) -> {
            int i = (int)Math.round(v.doubleValue());
            switch (i) {
                case 0 -> yaw = 35;           // U
                case 1 -> yaw = 155;          // L
                case 2 -> yaw = -85;          // R
                case 3 -> yaw = 245;          // B
            }
            updateCameraTransform();
        });

        HBox bar = new HBox(8, u, u2, ut, ut2, new Label("Face:"), faceSlider);
        setTop(bar);
        setCenter(subScene);

        // repaint when model changes
        repaint(state.get());
        state.addListener((obs, oldS, newS) -> repaint(newS));
    }

    // -------------------- geometry & camera --------------------

    /**
     * Builds the static geometry: 4 faces + layer/tip groups assembled to a pyramid.
     * We use unit-length triangles and then position/rotate them so all faces meet.
     */
    private void buildGeometry() {
        double side = 160;
        // One local face centered at origin, pointing "up" (its local +Z is outward normal)
        for (Face f : Face.values()) {
            Group face = new Group();
            faceGroup.put(f, face);

            Group layer = new Group(face);  // rotate layer about its face axis
            Group tip   = new Group();      // separate node for the small tip (rotates around vertex)
            layerGroup.put(f, layer);
            tipGroup.put(f, tip);

            // create stickers for this face (center + 3 edges + tip) in local face space
            buildFaceStickers(face, tip, f, side);

            // orient each face into a pyramid (regular tetrahedron-like orientation)
            // U: look toward viewer; L/R/B: rotate around Y and X to form the pyramid
            switch (f) {
                case U -> {
                    layer.getTransforms().addAll(
                            new Rotate(0, Rotate.Y_AXIS),
                            new Rotate(-60, Rotate.X_AXIS),
                            new Translate(0, 0, 0)
                    );
                    tip.getTransforms().addAll(layer.getTransforms());
                }
                case L -> {
                    layer.getTransforms().addAll(
                            new Rotate(120, Rotate.Y_AXIS),
                            new Rotate(-60, Rotate.X_AXIS)
                    );
                    tip.getTransforms().addAll(layer.getTransforms());
                }
                case R -> {
                    layer.getTransforms().addAll(
                            new Rotate(-120, Rotate.Y_AXIS),
                            new Rotate(-60, Rotate.X_AXIS)
                    );
                    tip.getTransforms().addAll(layer.getTransforms());
                }
                case B -> {
                    layer.getTransforms().addAll(
                            new Rotate(180, Rotate.Y_AXIS),
                            new Rotate(-60, Rotate.X_AXIS)
                    );
                    tip.getTransforms().addAll(layer.getTransforms());
                }
            }

            root3D.getChildren().addAll(layer, tip);
        }
    }

    /**
     * Creates one face’s stickers: a big center triangle, three edge triangles, and a small tip.
     * Stickers are simple TriangleMesh with flat Phong materials; colors are assigned in repaint().
     */
    private void buildFaceStickers(Group face, Group tip, Face f, double a) {
        double h = a * Math.sqrt(3) / 2.0;

        // Vertex positions: local 2D in X–Y plane; Z=0 (we'll rotate later)
        // A (0, -h/3*2) top-ish; B and C symmetrically at bottom; keep centered around origin
        float Ax = 0f,        Ay = (float)(-h * 2.0 / 3.0);
        float Bx = (float)(-a/2.0), By = (float)(h/3.0);
        float Cx = (float)( a/2.0), By2 = By;

        // Center triangle (smaller triangle around centroid)
        face.getChildren().add(triSticker( Ax*0.55f, Ay*0.55f,
                Bx*0.55f, By*0.55f,
                Cx*0.55f, By2*0.55f ));

        // Edge stickers: narrow triangles pointing inward from each side
        face.getChildren().add(triSticker( (Ax+Bx)/2f, (Ay+By)/2f, (Ax*0.35f+Bx*0.65f), (Ay*0.35f+By*0.65f), 0f, 0f ));
        face.getChildren().add(triSticker( (Bx+Cx)/2f, (By+By2)/2f, (Bx*0.65f+Cx*0.35f), (By*0.65f+By2*0.35f), 0f, 0f ));
        face.getChildren().add(triSticker( (Cx+Ax)/2f, (By2+Ay)/2f, (Cx*0.35f+Ax*0.65f), (By2*0.35f+Ay*0.65f), 0f, 0f ));

        // Tip: a tiny triangle near A
        tip.getChildren().add(triSticker( Ax, Ay,
                (Ax*0.8f+Bx*0.2f), (Ay*0.8f+By*0.2f),
                (Ax*0.8f+Cx*0.2f), (Ay*0.8f+By2*0.2f) ));
    }

    /** Utility: creates a flat triangular MeshView with a default gray material. */
    private MeshView triSticker(float x1, float y1, float x2, float y2, float x3, float y3) {
        TriangleMesh m = new TriangleMesh();
        m.getPoints().addAll(
                x1, y1, 0,   x2, y2, 0,   x3, y3, 0
        );
        // one dummy texCoord + one face
        m.getTexCoords().addAll(0,0);
        m.getFaces().addAll( 0,0, 1,0, 2,0 );
        MeshView mv = new MeshView(m);
        mv.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
        mv.setCullFace(CullFace.NONE);
        return mv;
    }

    private void setupCamera() {
        cam.setNearClip(0.1);
        cam.setFarClip(5000);
        updateCameraTransform();
    }

    private void updateCameraTransform() {
        // Place camera in spherical coords around origin
        root3D.getTransforms().setAll(
                new Rotate(yaw, 0, 0, 0, Rotate.Y_AXIS),
                new Rotate(pitch, 0, 0, 0, Rotate.X_AXIS)
        );
        cam.getTransforms().setAll(
                new Translate(0, 0, -distance)
        );
    }

    private void installOrbitControls() {
        subScene.setOnScroll(e -> {
            distance = Math.max(220, Math.min(1400, distance + (e.getDeltaY() > 0 ? -30 : 30)));
            updateCameraTransform();
        });
        subScene.setOnMousePressed(e -> { lastX = e.getSceneX(); lastY = e.getSceneY(); });
        subScene.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX, dy = e.getSceneY() - lastY;
            yaw += dx * 0.5;
            pitch = Math.max(-85, Math.min(85, pitch + dy * 0.5));
            lastX = e.getSceneX(); lastY = e.getSceneY();
            updateCameraTransform();
        });
    }

    // -------------------- painting & moves --------------------

    /**
     * Paints materials (colors) for stickers based on current state.
     * Why: visuals reflect where each edge piece belongs (piece tint).
     */
    private void repaint(PyraminxState s) {
        for (Face f : Face.values()) {
            Group face = faceGroup.get(f);
            Group tips = tipGroup.get(f);
            // Tip (index 0 in tips): face color
            tips.getChildren().forEach(n -> ((MeshView)n).setMaterial(new PhongMaterial(Palette.color(f))));

            // Face children order: center, then three edges
            int i = 0;
            for (Node n : face.getChildren()) {
                MeshView mv = (MeshView) n;
                if (i == 0) {
                    mv.setMaterial(new PhongMaterial(Palette.color(f)));
                } else {
                    EdgePos[] ring = StickerMap.edgesAround(f);
                    // piece-tint so scrambles are visible
                    Face tint = StickerMap.edgeStickerPieceTint(s, f, ring[i - 1]);
                    mv.setMaterial(new PhongMaterial(Palette.color(tint)));
                }
                i++;
            }
        }
    }

    /**
     * Animates a single move on the appropriate group; upon completion, calls moveSink.accept(m)
     * to mutate the model, then repaints the stickers with new colors.
     */
    private void animateMove(Move m) {
        Group target;
        Point3D axis;

        if (m instanceof LayerRotation lr) {
            target = layerGroup.get(lr.face());
            axis = new Point3D(0, 0, 1); // local face normal (after we oriented faces)
            rotate(target, axis, lr.turns() * 120, () -> {
                // commit to model and snap visual back (avoid cumulative float drift)
                moveSink.accept(m);
                target.getTransforms().removeIf(t -> t instanceof Rotate);
                repaint(state.get());
            });
        } else if (m instanceof TipRotation tr) {
            target = tipGroup.get(tr.face());
            axis = new Point3D(0, 0, 1);
            rotate(target, axis, tr.turns() * 120, () -> {
                moveSink.accept(m);
                target.getTransforms().removeIf(t -> t instanceof Rotate);
                repaint(state.get());
            });
        }
    }

    /** Plays a RotateTransition on a Group around an axis. */
    private void rotate(Group g, Point3D axis, double degrees, Runnable onFinished) {
        RotateTransition rt = new RotateTransition(Duration.millis(220), g);
        rt.setAxis(axis);
        rt.setByAngle(degrees);
        rt.setInterpolator(Interpolator.EASE_BOTH);
        rt.setOnFinished(e -> onFinished.run());
        rt.play();
    }
}