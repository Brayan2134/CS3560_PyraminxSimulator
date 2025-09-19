package org.example.io;

import org.example.model.PyraminxState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * GameIO.java
 * Desc: Tiny persistence helper for saving/loading a Pyraminx session.
 *
 * Design:
 *  - Writes a single-line, versioned snapshot (see PyraminxState.toSnapshot()).
 *  - Uses atomic "write to .tmp then move" to avoid half-written files.
 *  - Provides convenience autosave helpers that pick a default filename.
 *
 * Invariants:
 *  - File format is line-oriented, UTF-8, starts with "v:1;".
 *  - All parsing/validation is delegated to PyraminxState.fromSnapshot(...).
 */
public final class GameIO {

    /** Default filename used by autosave helpers. */
    public static final String DEFAULT_SAVE_BASENAME = "pyraminx.save";

    private GameIO() { }

    // ---------------------------------------------------------------------
    // Core API
    // ---------------------------------------------------------------------

    /**
     * Saves the given state to the target file (atomic replace).
     *
     * Preconditions:
     *  @param target != null
     *  @param state  != null
     *
     * Postconditions:
     *  - {@code target} exists and contains a single UTF-8 line produced by {@link PyraminxState#toSnapshot()}.
     *
     * @throws IOException if the filesystem operation fails (permissions, disk full, etc.)
     */
    public static void save(Path target, PyraminxState state) throws IOException {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(state, "state");

        // Ensure parent directory exists
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Write to sidecar temp then atomically move into place
        Path tmp = tmpSibling(target, ".tmp");
        String line = state.toSnapshot() + System.lineSeparator();
        Files.writeString(tmp, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Loads a state from the given file.
     *
     * Preconditions:
     *  @param source != null
     *  - {@code source} must exist and be readable
     *
     * Postconditions:
     *  - Returns a validated {@link PyraminxState} parsed by {@link PyraminxState#fromSnapshot(String)}.
     *
     * @throws IOException if reading the file fails
     * @throws IllegalArgumentException if the file contents are malformed/unsupported
     */
    public static PyraminxState load(Path source) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source, "source");
        String line = Files.readString(source, StandardCharsets.UTF_8);
        // Trim so trailing newline is harmless
        return PyraminxState.fromSnapshot(line.trim());
    }

    // ---------------------------------------------------------------------
    // Autosave helpers (optional sugar you can call from the controller)
    // ---------------------------------------------------------------------

    /**
     * Saves to {@code dir/DEFAULT_SAVE_BASENAME}.
     *
     * Preconditions:
     *  @param dir   != null
     *  @param state != null
     *
     * Postconditions:
     *  - File is created/replaced atomically in the given directory.
     */
    public static Path autosave(Path dir, PyraminxState state) throws IOException {
        Objects.requireNonNull(dir, "dir");
        return saveReturningPath(dir.resolve(DEFAULT_SAVE_BASENAME), state);
    }

    /**
     * Loads {@code dir/DEFAULT_SAVE_BASENAME} if present; otherwise returns {@code fallback.get()}.
     * Typical usage: on app start, pass {@code () -> PyraminxState.solved()}.
     *
     * Preconditions:
     *  @param dir      != null
     *  @param fallback != null and returns a non-null state
     *
     * Postconditions:
     *  - If the save file exists and is valid → returns it.
     *  - If missing or invalid → returns {@code fallback.get()}.
     */
    public static PyraminxState loadOrDefault(Path dir, Supplier<PyraminxState> fallback) {
        Objects.requireNonNull(dir, "dir");
        Objects.requireNonNull(fallback, "fallback");
        Path p = dir.resolve(DEFAULT_SAVE_BASENAME);
        if (Files.isRegularFile(p)) {
            try {
                return load(p);
            } catch (Exception ignored) {
                // Corrupted/old save → fall back cleanly
            }
        }
        return Objects.requireNonNull(fallback.get(), "fallback supplied null");
    }

    /**
     * Returns true if the file looks like a snapshot (cheap pre-check).
     * This is optional and not required by the core API.
     */
    public static boolean looksLikeSnapshot(Path p) {
        try {
            String first = Files.readString(p, StandardCharsets.UTF_8);
            return first != null && first.trim().startsWith("v:");
        } catch (IOException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------

    private static Path tmpSibling(Path target, String ext) {
        String name = target.getFileName().toString();
        return target.resolveSibling(name + ext);
    }

    private static Path saveReturningPath(Path target, PyraminxState s) throws IOException {
        save(target, s);
        return target;
    }
}
