// Filename: Config.java

package formatter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Configuration loader/persister.
 *
 * Features:
 *  - Preserves comments and blank lines.
 *  - Resolves <UNIT_NUMBER> token.
 *  - Loads the contents of every key ending with _file_address into a derived key
 *    whose name ends with FileContents (camelCase conversion).
 *  - Resolves relative paths against the directory that contains config.txt
 *    (improves portability vs depending on current working directory).
 *
 * Enhancement:
 *  - Missing or unreadable file paths now produce diagnostic markers instead of silent "".
 */
public final class Config {

    public static String unit;
	private interface Line { String raw(); }
    private record CommentLine(String raw) implements Line {}
    private record BlankLine(String raw) implements Line {}
    private record EntryLine(String key, String value, String rawPrefix, String rawSuffix) implements Line {
        @Override public String raw() { return rawPrefix + key + " = " + value + rawSuffix; }
    }

    private final List<Line> lines;
    private final LinkedHashMap<String,String> values;
    private final LinkedHashMap<String,String> derivedValues;
    private final Path sourcePath;
    private final Path configDir;

    private static final String MISSING_PREFIX = "[MISSING FILE:";
    private static final String UNREADABLE_PREFIX = "[UNREADABLE FILE:";

    private Config(List<Line> lines,
                   LinkedHashMap<String,String> values,
                   Path sourcePath) {
        this.lines = lines;
        this.values = values;
        this.derivedValues = new LinkedHashMap<>();
        this.sourcePath = sourcePath;
        this.configDir = sourcePath.toAbsolutePath().getParent();
        loadDerivedFileContents();
    }

    /* ------------ Loading / Reloading ------------ */

    public static Config load(String configFileName) throws IOException {
        Path p = Paths.get(configFileName).toAbsolutePath().normalize();
        if (!Files.isRegularFile(p)) {
            throw new FileNotFoundException("Config file not found: " + p);
        }
        List<String> rawLines = Files.readAllLines(p, StandardCharsets.UTF_8);
        List<Line> parsed = new ArrayList<>(rawLines.size());
        LinkedHashMap<String,String> kv = new LinkedHashMap<>();

        for (String raw : rawLines) {
            if (raw.trim().isEmpty()) {
                parsed.add(new BlankLine(raw));
                continue;
            }
            if (raw.trim().startsWith("#")) {
                parsed.add(new CommentLine(raw));
                continue;
            }
            int eq = raw.indexOf('=');
            if (eq < 0) {
                parsed.add(new CommentLine(raw));
                continue;
            }
            String left = raw.substring(0, eq).trim();
            String right = raw.substring(eq + 1).trim();
            kv.put(left, right);
            parsed.add(new EntryLine(left, right, "", ""));
        }

        return new Config(parsed, kv, p);
    }

    public Config reload() throws IOException {
        return load(sourcePath.toString());
    }

    /* ------------ Getters / Mutation ------------ */

    public String get(String key) {
        return values.get(key);
    }

    public void set(String key, String value) {
        boolean updated = false;
        for (int i=0; i<lines.size(); i++) {
            Line ln = lines.get(i);
            if (ln instanceof EntryLine el && el.key().equals(key)) {
                lines.set(i, new EntryLine(key, value, el.rawPrefix(), el.rawSuffix()));
                updated = true;
                break;
            }
        }
        if (!updated) {
            lines.add(new EntryLine(key, value, "", ""));
        }
        values.put(key, value);
    }

    public Path getConfigDir() {
        return configDir;
    }

    public String getResolved(String key) {
        String raw = values.get(key);
        if (raw == null) return null;
        return resolvePlaceholders(raw);
    }

    /**
     * Access to derived loaded file contents (the key is the camelCase + FileContents).
     * May now contain diagnostic markers such as:
     *   [MISSING FILE:<absolutePath>]
     *   [UNREADABLE FILE:<absolutePath>] <message>
     */
    public String get(String key, boolean derived) {
        return derived ? derivedValues.get(key) : values.get(key);
    }

    public Map<String,String> rawValues() {
        return Collections.unmodifiableMap(values);
    }

    public Map<String,String> derivedValues() {
        return Collections.unmodifiableMap(derivedValues);
    }

    public void save() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(sourcePath, StandardCharsets.UTF_8)) {
            for (Line l : lines) {
                bw.write(l.raw());
                bw.newLine();
            }
        }
    }

    @Override
    public String toString() {
        return "# Raw Values\n" + values;
    }

    public String toResolvedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Resolved Values").append(System.lineSeparator());
        for (String k : values.keySet()) {
            sb.append(k).append(" = ").append(getResolved(k)).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /* ------------ Diagnostic Helpers ------------ */

    public static boolean isMissingMarker(String s) {
        return s != null && s.startsWith(MISSING_PREFIX);
    }
    public static boolean isUnreadableMarker(String s) {
        return s != null && s.startsWith(UNREADABLE_PREFIX);
    }

    /* ------------ Internal Helpers ------------ */

    private void loadDerivedFileContents() {
        for (Map.Entry<String,String> entry : values.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith("_file_address")) continue;

            String substituted = resolvePlaceholders(entry.getValue());
            if (substituted == null || substituted.isBlank()) continue;

            Path resolvedPath = resolvePath(substituted);
            String contentKey = toContentKey(key);

            if (resolvedPath != null && Files.isRegularFile(resolvedPath) && Files.isReadable(resolvedPath)) {
                try {
                    String fileData = Files.readString(resolvedPath, StandardCharsets.UTF_8);
                    derivedValues.put(contentKey, fileData);
                } catch (IOException ioe) {
                    derivedValues.put(contentKey,
                            UNREADABLE_PREFIX + resolvedPath.toAbsolutePath() + "] " + ioe.getMessage());
                }
            } else {
                if (resolvedPath != null && !Files.exists(resolvedPath)) {
                    derivedValues.put(contentKey,
                            MISSING_PREFIX + resolvedPath.toAbsolutePath() + "]");
                } else {
                    // Exists but not readable or not a regular file
                    derivedValues.put(contentKey,
                            UNREADABLE_PREFIX + resolvedPath.toAbsolutePath() + "] Not a readable regular file");
                }
            }
        }
    }

    private Path resolvePath(String pathString) {
        Path p = Paths.get(pathString);
        if (!p.isAbsolute()) {
            p = configDir.resolve(pathString);
        }
        return p.normalize();
    }

    private String toContentKey(String fileAddressKey) {
        String base = fileAddressKey.substring(0, fileAddressKey.length() - "_file_address".length());
        String[] parts = base.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<parts.length;i++) {
            String part = parts[i];
            if (i==0) {
                sb.append(part);
            } else {
                sb.append(part.substring(0,1).toUpperCase()).append(part.substring(1));
            }
        }
        sb.append("FileContents");
        return sb.toString();
    }

    private String resolvePlaceholders(String input) {
        if (input == null) return null;
        if (input.contains("<UNIT_NUMBER>")) {
            String unit = values.get("unit");
            if (unit != null) input = input.replace("<UNIT_NUMBER>", unit);
        }
        return input;
    }
}