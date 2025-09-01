package formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Mutable configuration with:
 *  - key = value entries
 *  - comment & blank line preservation
 *  - runtime placeholder resolution for <UNIT_NUMBER> only
 *
 * Comments begin with '#' or '//' after optional leading whitespace.
 * Blank lines are preserved.
 */
public final class Config {

    /* ---------- Internal line model ---------- */
    private enum LineType { ENTRY, COMMENT, BLANK }

    private static final class Line {
        LineType type;
        String key;     // only for ENTRY
        String value;   // only for ENTRY (raw, may contain placeholders)
        String text;    // original text for COMMENT, or blank line
        Line(LineType type, String key, String value, String text) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.text = text;
        }
        static Line entry(String key, String value) {
            return new Line(LineType.ENTRY, key, value, null);
        }
        static Line comment(String text) {
            return new Line(LineType.COMMENT, null, null, text);
        }
        static Line blank() {
            return new Line(LineType.BLANK, null, null, "");
        }
    }

    private final List<Line> lines;                // ordered source lines
    private final LinkedHashMap<String,String> values; // maps keys to (current) values
    private final Path sourcePath;

    private Config(List<Line> lines, LinkedHashMap<String,String> values, Path sourcePath) {
        this.lines = lines;
        this.values = values;
        this.sourcePath = sourcePath;
    }

    /* ---------- Loading ---------- */

    public static Config load(String path) throws IOException {
        return load(Path.of(path));
    }

    public static Config load(Path path) throws IOException {
        List<Line> lines = new ArrayList<>();
        LinkedHashMap<String,String> map = new LinkedHashMap<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String rawLine;
            while ((rawLine = br.readLine()) != null) {
                String trimmed = rawLine.trim();
                if (trimmed.isEmpty()) {
                    lines.add(Line.blank());
                    continue;
                }
                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    lines.add(Line.comment(rawLine));
                    continue;
                }
                int eq = rawLine.indexOf('=');
                if (eq < 0) {
                    // Treat malformed line as a comment to avoid data loss
                    lines.add(Line.comment(rawLine));
                    continue;
                }
                // Split around '=' but allow spaces; we normalize on save.
                String left = rawLine.substring(0, eq).trim();
                String right = rawLine.substring(eq + 1).trim();
                map.put(left, right);
                lines.add(Line.entry(left, right));
            }
        }
        return new Config(lines, map, path);
    }

    /* ---------- Accessors ---------- */

    /** Raw (unresolved) value */
    public String get(String key) {
        return values.get(key);
    }

    /** Resolve placeholders for a single key */
    public String getResolved(String key) {
        String raw = get(key);
        if (raw == null) return null;
        return resolvePlaceholders(raw);
    }

    /** Unmodifiable view of raw map */
    public Map<String,String> asRawMap() {
        return Collections.unmodifiableMap(values);
    }

    /** All values with placeholders resolved */
    public Map<String,String> resolvedMap() {
        LinkedHashMap<String,String> out = new LinkedHashMap<>();
        for (Map.Entry<String,String> e : values.entrySet()) {
            out.put(e.getKey(), resolvePlaceholders(e.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    /* ---------- Mutation ---------- */

    /**
     * Set key to value (or remove if value == null).
     * Preserves file ordering; new keys appended at end (with a separating blank line if last line not blank).
     */
    public void set(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null/blank");
        }
        Line existing = null;
        for (Line line : lines) {
            if (line.type == LineType.ENTRY && line.key.equals(key)) {
                existing = line;
                break;
            }
        }
        if (value == null) {
            // Remove
            values.remove(key);
            if (existing != null) {
                existing.type = LineType.COMMENT;
                existing.text = "# (removed) " + existing.key + " = " + existing.value;
                existing.key = null;
                existing.value = null;
            }
            return;
        }
        if (existing != null) {
            existing.value = value;
            values.put(key, value);
        } else {
            // Append new entry (start a new section if last line was an entry/comment)
            if (!lines.isEmpty()) {
                Line last = lines.get(lines.size() - 1);
                if (last.type == LineType.ENTRY || last.type == LineType.COMMENT) {
                    lines.add(Line.blank());
                }
            }
            Line line = Line.entry(key, value);
            lines.add(line);
            values.put(key, value);
        }
    }

    /* ---------- Save / Reload ---------- */

    /** Save back to original path */
    public void save() throws IOException {
        if (sourcePath == null) {
            throw new IllegalStateException("No sourcePath; use save(Path) instead.");
        }
        save(sourcePath);
    }

    /** Save to a specific path */
    public void save(Path path) throws IOException {
        try (Writer w = Files.newBufferedWriter(path)) {
            for (Line line : lines) {
                switch (line.type) {
                    case BLANK -> w.write(System.lineSeparator());
                    case COMMENT -> {
                        w.write(line.text);
                        w.write(System.lineSeparator());
                    }
                    case ENTRY -> {
                        // Normalize format: key = value
                        w.write(line.key + " = " + (line.value == null ? "" : line.value));
                        w.write(System.lineSeparator());
                    }
                }
            }
        }
    }

    /** Reload from disk (discarding any unsaved changes) */
    public Config reload() {
        if (sourcePath == null) throw new IllegalStateException("No sourcePath to reload.");
        try {
            return load(sourcePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to reload " + sourcePath, e);
        }
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    /* ---------- Placeholder resolution ---------- */

    private String resolvePlaceholders(String input) {
        if (input == null) return null;
        if (input.indexOf('<') >= 0) {
            String unit = values.get("unit");
            if (unit != null) {
                input = input.replace("<UNIT_NUMBER>", unit);
            }
            // NOTE: Theme placeholder intentionally NOT implemented now.
            // If later you add <THEME>, just uncomment:
            // String theme = values.get("theme");
            // if (theme != null) input = input.replace("<THEME>", theme);
        }
        return input;
    }

    /* ---------- Debug Strings ---------- */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Config (raw){\n");
        values.forEach((k,v) -> sb.append("  ").append(k).append(" = ").append(v).append('\n'));
        sb.append('}');
        return sb.toString();
    }

    public String toResolvedString() {
        StringBuilder sb = new StringBuilder("Config (resolved){\n");
        resolvedMap().forEach((k,v) -> sb.append("  ").append(k).append(" = ").append(v).append('\n'));
        sb.append('}');
        return sb.toString();
    }
}