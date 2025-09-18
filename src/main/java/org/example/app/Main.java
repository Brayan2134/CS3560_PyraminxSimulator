package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.controller.PuzzleController;
import org.example.model.engine.History;
import org.example.view.FxRenderer;
import org.example.view.InputBindings;

/**
 * Main.java
 * Desc: Small JavaFX shell to wire controller + renderer.
 * Role: App boot (UI only)
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Controller owns the model state + history
        PuzzleController controller = new PuzzleController(new History());

        // Top bar controls
        Button scramble = new Button("Scramble");
        Button undo     = new Button("Undo");
        Button redo     = new Button("Redo");
        Button reset    = new Button("Reset");

        scramble.setOnAction(e -> controller.scramble(12));
        undo.setOnAction(e -> controller.undo());
        redo.setOnAction(e -> controller.redo());
        reset.setOnAction(e -> controller.reset());

        ToolBar bar = new ToolBar(scramble, undo, redo, reset);

        // Renderer subscribes to the controller's state
        FxRenderer renderer = new FxRenderer(controller.stateProperty());

        BorderPane root = new BorderPane(renderer);
        root.setTop(bar);

        Scene scene = new Scene(root, 720, 480);
        InputBindings.install(scene, controller); // keyboard: U/L/R/B + tips

        stage.setTitle("Pyraminx Simulator (diagnostic)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
