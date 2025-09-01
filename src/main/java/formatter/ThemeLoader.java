package formatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal, dependency-free loader for the constrained JSON structure used by theme files.
 * NOT a general JSON parser. Assumes well-formed theme JSON.
 */
public final class ThemeLoader {

    private ThemeLoader() {}

    public static Theme load(String themeName) {
        if (themeName == null || themeName.isBlank()) {
            System.out.println("Theme name is blank.");
            return null;
        }
        Path path = Path.of("themes", themeName + ".json");
        if (!Files.isRegularFile(path)) {
            System.out.println("Theme file not found: " + path);
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

    private static Theme parseTheme(String json) {
        // Remove CR, trim
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

        // Very naive: split on quotes around keys.
        // Pattern: "token": "css;..."
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
     * Extracts a simple string value "key": "value"
     */
    private static String extractString(String src, String keyLiteral) {
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
     * Extracts the JSON object text for "key": { ... }
     */
    private static String extractObject(String src, String keyLiteral) {
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
}