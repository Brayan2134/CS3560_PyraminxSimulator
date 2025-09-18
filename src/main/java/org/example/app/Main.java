package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.controller.PuzzleController;
import org.example.model.engine.History;
import org.example.model.moves.MoveLibrary;
import org.example.view.Animator;
import org.example.view.FxRenderer;
import org.example.view.InputBindings;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.example.model.moves.MoveLibrary;
import org.example.view.Animator;

/**
 * Main.java
 * Desc: Small JavaFX shell to wire controller + renderer.
 * Role: App boot (UI only)
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        PuzzleController controller = new PuzzleController(new History());

        Button scramble = new Button("Scramble");
        Button undo     = new Button("Undo");
        Button redo     = new Button("Redo");
        Button reset    = new Button("Reset");
        Button solve    = new Button("Solve");

        // algorithm input + actions
        TextField alg = new TextField();
        alg.setPromptText("Type alg: e.g., U L' r2 u");
        Button applyAlg = new Button("Apply");
        Button playAlg  = new Button("Play");


        scramble.setOnAction(e -> controller.scramble(12));
        undo.setOnAction(e -> controller.undo());
        redo.setOnAction(e -> controller.redo());
        reset.setOnAction(e -> controller.reset());
        solve.setOnAction(e -> controller.solveByUndoAll());

        applyAlg.setOnAction(e -> controller.applyAlg(alg.getText()));
        playAlg.setOnAction(e -> Animator.play(controller, MoveLibrary.parse(alg.getText()), Duration.millis(180)));
        alg.setOnAction(e -> controller.applyAlg(alg.getText())); // press Enter to apply

        Label legend = new Label("• Click=Layer  • Shift=CCW  • Alt/Ctrl=Tip  • RightClick=CCW");
        legend.setStyle("-fx-text-fill: #555;");
        ToolBar bar = new ToolBar(scramble, undo, redo, reset, solve, new Separator(),
                new Label("Alg:"), alg, applyAlg, playAlg,
                new Separator(), legend);


        FxRenderer renderer = new FxRenderer(controller.stateProperty(), controller::apply);
        BorderPane root = new BorderPane(renderer); root.setTop(bar);
        Scene scene = new Scene(root, 800, 520);
        InputBindings.install(scene, controller);
        stage.setTitle("Pyraminx Simulator (diagnostic)"); stage.setScene(scene); stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
