import org.example.controller.PuzzleController;
import org.example.io.GameIO;
import org.example.model.PyraminxState;
import org.example.model.engine.History;
import org.example.model.moves.MoveLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AutosaveTests.java
 * Desc: Integration-style tests for controller autosave + load-at-startup.
 * Role: Verifies that each public mutation writes an up-to-date snapshot,
 *       and that startup load handles both valid and corrupted saves.
 *
 * Test strategy:
 *  - Redirect autosave location by setting user.home to a JUnit temp dir
 *    BEFORE creating the controller (so its autosaveDir is under @TempDir).
 *  - After each operation, read ~/.pyraminx/pyraminx.save and compare with
 *    the controller's current state snapshot.
 */
public class AutosaveTests {

    @TempDir
    Path tmpHome; // JUnit creates/cleans this folder

    private Path autosaveDir;
    private Path autosaveFile;
    private PuzzleController controller;

    @BeforeEach
    void setup() throws Exception {
        // Redirect user.home so the controller writes to @TempDir
        System.setProperty("user.home", tmpHome.toString());
        autosaveDir  = tmpHome.resolve(".pyraminx");
        autosaveFile = autosaveDir.resolve(GameIO.DEFAULT_SAVE_BASENAME);

        // Fresh controller per test
        controller = new PuzzleController(new History());

        // Initialize from autosave (none yet) -> solved
        controller.initFromAutosaveOrSolved();
        // Sanity: after init, file should exist (replaceState autosaves)
        assertTrue(Files.exists(autosaveFile), "Autosave after init should create the file");
    }

    /** Helper: read snapshot line from autosave file */
    private String readSaved() throws Exception {
        return Files.readString(autosaveFile, StandardCharsets.UTF_8).trim();
    }

    /** Helper: current snapshot from controller */
    private String currentSnap() {
        return controller.stateProperty().get().toSnapshot();
    }

    @Test
    void autosave_onApply_writesCurrentSnapshot() throws Exception {
        controller.applyAlg("U");
        assertEquals(currentSnap(), readSaved(), "Autosave must match post-apply state");
    }

    @Test
    void autosave_onUndo_and_onRedo() throws Exception {
        controller.applyAlg("U R");
        String afterApply = currentSnap();
        assertEquals(afterApply, readSaved(), "After apply, autosave should be updated");

        controller.undo();
        String afterUndo = currentSnap();
        assertEquals(afterUndo, readSaved(), "After undo, autosave should reflect undone state");

        controller.redo();
        String afterRedo = currentSnap();
        assertEquals(afterRedo, readSaved(), "After redo, autosave should reflect redone state");
    }

    @Test
    void autosave_onReset() throws Exception {
        controller.applyAlg("U R u2");
        controller.reset();
        assertEquals(PyraminxState.solved().toSnapshot(), readSaved(), "Reset should autosave solved snapshot");
    }

    @Test
    void autosave_onScramble() throws Exception {
        controller.scramble(8);
        assertEquals(currentSnap(), readSaved(), "Scramble should autosave scrambled snapshot");
    }

    @Test
    void autosave_onSolveByUndoAll() throws Exception {
        controller.applyAlg("U R L");
        controller.solveByUndoAll(); // uses undo chain until solved
        assertEquals(PyraminxState.solved().toSnapshot(), readSaved(), "solveByUndoAll should autosave solved snapshot");
    }

    @Test
    void loadAtStartup_readsValidAutosave() throws Exception {
        // Create a known non-solved state and autosave is already wired in controller
        controller.applyAlg("U R");
        String saved = readSaved();

        // New controller instance simulates app restart
        PuzzleController c2 = new PuzzleController(new History());
        // user.home still points to tmpHome
        c2.initFromAutosaveOrSolved();

        assertEquals(saved, c2.stateProperty().get().toSnapshot(), "Startup should load the existing autosave");
    }

    @Test
    void loadAtStartup_fallsBackOnCorruptedFile() throws Exception {
        // Corrupt the autosave file BEFORE creating the new controller
        Files.createDirectories(autosaveDir);
        Files.writeString(autosaveFile, "v:1; tipOri:[oops]", StandardCharsets.UTF_8);

        // New controller simulates app restart; should fall back to solved
        PuzzleController c2 = new PuzzleController(new History());
        c2.initFromAutosaveOrSolved();

        assertEquals(PyraminxState.solved().toSnapshot(),
                c2.stateProperty().get().toSnapshot(),
                "Corrupted autosave should fall back to solved");
    }
}
