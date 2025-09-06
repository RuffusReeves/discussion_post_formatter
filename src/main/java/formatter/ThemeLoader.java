// Current filename: ThemeLoader.java

package formatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Minimal, dependency-free loader for the constrained JSON structure used by theme files.
 * NOT a general JSON parser. Assumes well-formed theme JSON.
 *
 * Added: listAvailableThemeNames() - scans the 'themes' directory for *.json files
 * and returns their theme names (prefers the internal "name" field from JSON if present).
 */
public final class ThemeLoader {

    private ThemeLoader() {}

    /**
     * Load a theme by name from themes/<themeName>.json
     */
    public static Theme load(String themeName) {
        if (themeName == null || themeName.isBlank()) {
            return null;
        }
        Path path = Path.of("themes", themeName + ".json");
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return parseTheme(json);
        } catch (IOException e) {
            System.out.println("Failed to read theme file: " + e.getMessage());
            return null;
        } catch (RuntimeException e) {
            System.out.println("Failed to parse theme file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scan the themes directory for *.json files and return a list of theme names.
     * For each file:
     *  - Try to parse it to obtain its internal "name"
     *  - If parsing fails or "name" missing, fall back to the filename (without extension)
     *
     * Order: alphabetical by theme name (case-insensitive).
     */
    public static List<String> listAvailableThemeNames() {
        Path dir = Path.of("themes");
        if (!Files.isDirectory(dir)) {
            return List.of(); // No themes directory present
        }

        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : ds) {
                String baseName = stripExt(p.getFileName().toString());
                String json;
                try {
                    json = Files.readString(p, StandardCharsets.UTF_8);
                } catch (IOException ioe) {
                    System.out.println("Skipping unreadable theme file: " + p + " (" + ioe.getMessage() + ")");
                    continue;
                }
                String parsedName = null;
                try {
                    parsedName = extractString(json, "\"name\"");
                } catch (Exception ignored) {
                    // Ignore parsing errors for name field; fallback to filename
                }
                if (parsedName == null || parsedName.isBlank()) {
                    parsedName = baseName;
                }
                names.add(parsedName);
            }
        } catch (IOException e) {
            System.out.println("Theme directory scan failed: " + e.getMessage());
        }

        // Sort case-insensitively for consistent menu ordering
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /* ----------------- Internal Parsing ----------------- */

    private static Theme parseTheme(String json) {
        String src = json.replace("\r", "").trim();

        String name = extractString(src, "\"name\"");
        String description = extractString(src, "\"description\"");
        String background = extractString(src, "\"background\"");
        String foreground = extractString(src, "\"foreground\"");
        String stylesObject = extractObject(src, "\"styles\"");
        Map<String,String> styles = parseStyles(stylesObject);

        return new Theme(name, description, background, foreground, styles);
    }

    private static Map<String,String> parseStyles(String stylesJson) {
        Map<String,String> map = new LinkedHashMap<>();
        if (stylesJson == null || stylesJson.isBlank()) return map;

        int idx = 0;
        while (idx < stylesJson.length()) {
            int keyStart = stylesJson.indexOf('"', idx);
            if (keyStart < 0) break;
            int keyEnd = stylesJson.indexOf('"', keyStart + 1);
            if (keyEnd < 0) break;
            String key = stylesJson.substring(keyStart + 1, keyEnd);

            int colon = stylesJson.indexOf(':', keyEnd);
            if (colon < 0) break;
            int valueStart = stylesJson.indexOf('"', colon);
            if (valueStart < 0) break;
            int valueEnd = stylesJson.indexOf('"', valueStart + 1);
            if (valueEnd < 0) break;
            String value = stylesJson.substring(valueStart + 1, valueEnd);

            map.put(key, value);
            idx = valueEnd + 1;
        }
        return map;
    }

    /**
     * Extract "key": "value"
     */
    static String extractString(String src, String keyLiteral) {
        int k = src.indexOf(keyLiteral);
        if (k < 0) return null;
        int colon = src.indexOf(':', k + keyLiteral.length());
        if (colon < 0) return null;
        int q1 = src.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = src.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return src.substring(q1 + 1, q2);
    }

    /**
     * Extract "key": { ... }
     */
    static String extractObject(String src, String keyLiteral) {
        int k = src.indexOf(keyLiteral);
        if (k < 0) return null;
        int colon = src.indexOf(':', k + keyLiteral.length());
        if (colon < 0) return null;
        int braceStart = src.indexOf('{', colon + 1);
        if (braceStart < 0) return null;
        int depth = 0;
        for (int i = braceStart; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return src.substring(braceStart, i + 1);
                }
            }
        }
        return null;
    }

    private static String stripExt(String fn) {
        int dot = fn.lastIndexOf('.');
        return (dot > 0) ? fn.substring(0, dot) : fn;
    }
}