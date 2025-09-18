package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.example.controller.PuzzleController;
import org.example.model.engine.History;
import org.example.view.FxRenderer;
import org.example.view.InputBindings;

/**
 * Main.java
 * Desc: JavaFX shell that wires the controller, renderer, and toolbar UI.
 * Role: App boot (UI only)
 *
 * Invariants:
 *  - View never mutates model directly; all mutations go through PuzzleController.
 *  - Renderer observes stateProperty() and repaints on change.
 */
public class Main extends Application {

    /**
     * Launches the JavaFX window, builds toolbar + renderer, and installs key bindings.
     * Preconditions:
     *  @param stage is provided by JavaFX runtime and is non-null.
     *
     * Postconditions:
     *  - A window is shown with toolbar actions (Scramble/Undo/Redo/Reset/Solve/Apply/Help).
     *  - Keyboard bindings (U/L/R/B and tips u/l/r/b) are active via InputBindings.
     */
    @Override
    public void start(Stage stage) {
        // Controller (owns state + history)
        PuzzleController controller = new PuzzleController(new History());

        // --- Toolbar: core actions ---
        Button scramble = new Button("Scramble");
        Button undo     = new Button("Undo");
        Button redo     = new Button("Redo");
        Button reset    = new Button("Reset");
        Button solve    = new Button("Solve");

        scramble.setOnAction(e -> controller.scramble(12));
        undo.setOnAction(e -> controller.undo());
        redo.setOnAction(e -> controller.redo());
        reset.setOnAction(e -> controller.reset());
        solve.setOnAction(e -> controller.solveByUndoAll());

        // --- Algorithm entry (Apply only; Play removed) ---
        TextField alg = new TextField();
        alg.setPromptText("Type alg: e.g., U L' r2 u");
        Button applyAlg = new Button("Apply");
        applyAlg.setOnAction(e -> controller.applyAlg(alg.getText()));
        alg.setOnAction(e -> controller.applyAlg(alg.getText())); // press Enter to apply

        // Move counter
        Label movesLabel = new Label();
        movesLabel.textProperty().bind(controller.moveCountProperty().asString("Moves: %d"));

        // Help
        Button help = new Button("Help");
        help.setOnAction(e -> showHelp(stage));

        // Toolbar layout
        ToolBar bar = new ToolBar(
                scramble, undo, redo, reset, solve,
                new Separator(),
                new Label("Alg:"), alg, applyAlg,
                new Separator(),
                movesLabel,
                new Separator(),
                help
        );

        // Renderer observes state; clicks send moves back through controller
        FxRenderer renderer = new FxRenderer(controller.stateProperty(), controller::apply);

        BorderPane root = new BorderPane(renderer);
        root.setTop(bar);

        Scene scene = new Scene(root, 1280, 800);
        InputBindings.install(scene, controller);

        // F1 opens Help
        scene.getAccelerators().put(KeyCombination.valueOf("F1"), () -> showHelp(stage));

        stage.setTitle("Pyraminx Simulator");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Shows a quick-reference dialog explaining controls, notation, and toolbar.
     * Why: onboard a new user and document the UI affordances.
     */
    private void showHelp(Stage owner) {
        String text = """
            Welcome to the Pyraminx Simulator!

            GOAL
            • It’s a 4-face pyramid puzzle. Solve by returning all faces to a single color.

            BASIC CONTROLS
            • Click a face: layer turn clockwise (120°).
            • Shift+Click or Right-Click: layer turn counterclockwise (CCW).
            • Alt or Ctrl + Click: tip turn (only the little corner).
            • Keyboard: U/L/R/B for layers, lowercase u/l/r/b for tips.
              Hold Shift with those keys for CCW.

            TOOLBAR
            • Scramble: random sequence.
            • Undo / Redo: step through your history.
            • Reset: return to solved.
            • Solve: undo all moves to solved.
            • Alg + Apply: type a sequence (e.g.,  U L' r2 u  ) and apply it.
            • Moves: live move counter.

            NOTATION
            • Uppercase = layer turn (affects edges + that face’s center).
            • Lowercase = tip turn (affects only the tip).
            • A bare move (U) is CW; “2” or “'” means CCW (U2, U').
            • Three turns of the same move is identity:  U U U  → no change.

            VIEWS
            • Net view shows each face separately (physical coloring).
            """;

        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefColumnCount(60);
        area.setPrefRowCount(22);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);
        alert.setTitle("How to Play");
        alert.setHeaderText("Pyraminx Simulator — Quick Guide");
        alert.getDialogPane().setContent(area);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
