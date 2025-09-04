package formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Config
 * ------
 * This class loads and manages the settings in config.txt.
 * Goals:
 *  - Keep original comments and blank lines (so file stays readable).
 *  - Let you read and update key=value pairs.
 *  - Replace <UNIT_NUMBER> only when a resolved value is requested
 *    (we do NOT rewrite the token inside config.txt).
 *  - Automatically load file contents for keys that contain "address"
 *    (except output_file_address) and expose them as derived values.
 *
 * Terms:
 *  "raw value"  = exactly what is in the file
 *  "resolved"   = same value but with <UNIT_NUMBER> replaced
 *  "derived"    = extra in-memory values created from reading files
 *
 * You DO NOT need to understand every detail right away—focus first
 * on how load(), get(), set(), and save() work.
 */
public final class Config {

    /* ---------- Internal line model ---------- */
    // We classify each line in the config file so we can write it back
    // exactly (with the same comments and spacing).
    private enum LineType { ENTRY, COMMENT, BLANK }

    /**
     * Represents one line from config.txt.
     * For ENTRY lines we store the key and value separately.
     * For COMMENT and BLANK lines we just keep the original text.
     */
    private static final class Line {
    	// Declare the Fields.
        LineType type;
        String key;     // for ENTRY
        String value;   // for ENTRY
        String text;    // original comment line text or "" for blank

        Line(LineType type, String key, String value, String text) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.text = text;
        }
        static Line entry(String key, String value) { return new Line(LineType.ENTRY, key, value, null); }
        static Line comment(String text) { return new Line(LineType.COMMENT, null, null, text); }
        static Line blank() { return new Line(LineType.BLANK, null, null, ""); }
    }

    // lines keeps the order and spacing of the original file.
    private final List<Line> lines;
    // values maps keys (like "unit") to their raw string value.
    private final LinkedHashMap<String,String> values;
    // derivedValues holds file contents loaded based on "*address" keys.
    private final LinkedHashMap<String,String> derivedValues;
    // Where the file came from (used when saving).
    private final Path sourcePath;

    private Config(List<Line> lines, LinkedHashMap<String,String> values, Path sourcePath) {
        this.lines = lines;
        this.values = values;
        this.derivedValues = new LinkedHashMap<>();
        this.sourcePath = sourcePath;
        // Load extra contents right after reading the config file.
        loadDerivedFileContents();
    }

    /* ---------- Loading ---------- */

    /**
     * Load config from a string path helper method.
     */
    public static Config load(String path) throws IOException {
        return load(Path.of(path));
    }

    /**
     * Load config from a Path.
     * Reads the file line by line, classifies each line, and stores key/value pairs.
     */
    public static Config load(Path path) throws IOException {
        List<Line> lines = new ArrayList<>();
        LinkedHashMap<String,String> map = new LinkedHashMap<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String rawLine;
            while ((rawLine = br.readLine()) != null) {
                String trimmed = rawLine.trim();

                // Blank line
                if (trimmed.isEmpty()) {
                    lines.add(Line.blank());
                    continue;
                }
                // Comment line (starts with # or //)
                if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    lines.add(Line.comment(rawLine));
                    continue;
                }
                // If there's no '=', treat as a comment to avoid losing it.
                int eq = rawLine.indexOf('=');
                if (eq < 0) {
                    lines.add(Line.comment(rawLine));
                    continue;
                }

                // Split into key = value (everything after first '=' belongs to value)
                String left = rawLine.substring(0, eq).trim();
                String right = rawLine.substring(eq + 1).trim();

                map.put(left, right);
                lines.add(Line.entry(left, right));
            }
        }
        return new Config(lines, map, path);
    }

    /* ---------- Accessors ---------- */

    /**
     * Returns the raw (unresolved) value for a key OR a derived value.
     * Example: get("unit") -> "3"
     */
    public String get(String key) {
        String value = values.get(key);
        if (value != null) return value;
        return derivedValues.get(key); // fallback to derived (like assignmentTextFileContents)
    }

    /**
     * Returns the value with placeholders replaced.
     * Currently only <UNIT_NUMBER>.
     */
    public String getResolved(String key) {
        String raw = get(key);
        if (raw == null) return null;
        return resolvePlaceholders(raw);
    }

    /**
     * Returns a read-only view of the original key/value pairs.
     * (Derived values are not included here.)
     */
    public Map<String,String> asRawMap() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Returns a new map with all placeholders resolved.
     */
    public Map<String,String> resolvedMap() {
        LinkedHashMap<String,String> out = new LinkedHashMap<>();
        for (Map.Entry<String,String> e : values.entrySet()) {
            out.put(e.getKey(), resolvePlaceholders(e.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    /* ---------- Mutation ---------- */

    /**
     * Sets or updates a key.
     * If value == null we "remove" it by converting its line to a comment (soft delete).
     * New keys are appended at the end (with a blank line separator for readability).
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
            // Remove case: convert to a comment so we don't lose history.
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
            // Update existing entry.
            existing.value = value;
            values.put(key, value);
        } else {
            // Add new key at end. Insert a blank line first if last line was content.
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

    /**
     * Saves back to the original file path.
     */
    public void save() throws IOException {
        if (sourcePath == null) {
            throw new IllegalStateException("No sourcePath; use save(Path) instead.");
        }
        save(sourcePath);
    }

    /**
     * Saves to a specific file path.
     * Rebuilds each line based on its type while keeping comments / blanks.
     */
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
                        w.write(line.key + " = " + (line.value == null ? "" : line.value));
                        w.write(System.lineSeparator());
                    }
                }
            }
        }
    }

    /**
     * Discards current in-memory data and reloads from disk.
     */
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

    /**
     * Replace runtime tokens like <UNIT_NUMBER> in a string value.
     * (We only add more tokens if we intentionally support them.)
     */
    private String resolvePlaceholders(String input) {
        if (input == null) return null;
        if (input.contains("<UNIT_NUMBER>")) {
            String unit = values.get("unit");
            if (unit != null) {
                input = input.replace("<UNIT_NUMBER>", unit);
            }
        }
        return input;
    }

    /* ---------- Derived file contents loading ---------- */

    /**
     * These keys are NOT loaded as file contents even if they contain 'address'.
     * For example, output_file_address points to an output file we will WRITE,
     * not something we want to read into memory.
     */
    private static final Set<String> EXCLUDED_ADDRESS_KEYS = Set.of(
            "output_file_address"
    );

    /**
     * Auto-load each file referenced by keys containing "address" (broad rule).
     * Then store the content under a camelCase "contents" key.
     * Example: assignment_text_file_address -> assignmentTextFileContents
     */
    private void loadDerivedFileContents() {
        for (Map.Entry<String,String> entry : values.entrySet()) {
            String key = entry.getKey();

            if (EXCLUDED_ADDRESS_KEYS.contains(key)) {
                continue; // skip output path
            }

            if (key.contains("address")) {
                String derivedKey = generateDerivedKey(key);

                // Avoid overwriting or weird empty names.
                if (derivedKey.isEmpty() || derivedKey.equals(key) || values.containsKey(derivedKey)) {
                    continue;
                }

                String filePath = resolvePlaceholders(entry.getValue());
                if (filePath != null) {
                    try {
                        if (Utils.fileExists(filePath)) {
                            String fileContent = Utils.readFile(filePath);
                            derivedValues.put(derivedKey, fileContent);
                        } else {
                            System.out.println("Warning: File not found: " + filePath + " (key: " + key + ")");
                        }
                    } catch (Exception e) {
                        System.out.println("Warning: Could not read: " + filePath + " (key: " + key + "): " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Turns original key into a derived camelCase key replacing 'address' with 'contents'.
     * Example: my_file_address -> myFileContents
     */
    private String generateDerivedKey(String originalKey) {
        String withContents = originalKey.replace("address", "contents");
        String[] parts = withContents.split("_");
        if (parts.length == 1) return withContents;

        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                camelCase.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    camelCase.append(parts[i].substring(1));
                }
            }
        }
        return camelCase.toString();
    }

    /* ---------- Debug Strings ---------- */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Config (raw){\n");
        values.forEach((k,v) -> sb.append("  ").append(k).append(" = ").append(v).append('\n'));

        if (!derivedValues.isEmpty()) {
            sb.append("\n  // Derived file contents (not persisted):\n");
            derivedValues.forEach((k,v) -> {
                String preview = v.length() > 50 ? v.substring(0, 50) + "..." : v;
                preview = preview.replace("\n", "\\n").replace("\r", "\\r");
                sb.append("  * ").append(k).append(" = ").append(preview).append('\n');
            });
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Same as toString() but shows resolved (placeholder-substituted) values.
     */
    public String toResolvedString() {
        StringBuilder sb = new StringBuilder("Config (resolved){\n");
        resolvedMap().forEach((k,v) -> sb.append("  ").append(k).append(" = ").append(v).append('\n'));

        if (!derivedValues.isEmpty()) {
            sb.append("\n  // Derived file contents (not persisted):\n");
            derivedValues.forEach((k,v) -> {
                String preview = v.length() > 50 ? v.substring(0, 50) + "..." : v;
                preview = preview.replace("\n", "\\n").replace("\r", "\\r");
                sb.append("  * ").append(k).append(" = ").append(preview).append('\n');
            });
        }
        sb.append('}');
        return sb.toString();
    }
}