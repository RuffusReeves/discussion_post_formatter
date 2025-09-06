// Current filename: Highlighter.java

package formatter;

import java.util.*;

/**
 * Java-like syntax highlighter.
 *
 * Now supports BOTH:
 *  - Built-in palettes (default, dark, tango)
 *  - External JSON themes (themes/<name>.json) loaded via ThemeLoader
 *
 * Behavior:
 *  1. When highlight(...) is called we first try to load an external Theme whose name
 *     matches themeName. If found, its styles drive the colors.
 *  2. If no external theme exists, we fall back to an internal palette.
 *  3. Missing individual token styles in an external theme fall back to:
 *       - theme.styles.get("default") if present
 *       - theme.foreground
 *       - THEN internal palette token color (if available)
 *       - Finally a neutral color (#222 or #d4d4d4 depending on palette)
 *
 * Recognized token style keys (for external theme JSON):
 *  keyword, type, string, char, comment, number, annotation, ident, default
 *
 * NOTE:
 *  - Punctuation and whitespace are emitted as-is (escaped) without a span.
 *  - Only tokens we classify get <span> wrappers.
 */
public final class Highlighter {

    private Highlighter() {}

    /* -------------------------------------------------------
     * Token classification sets (simple heuristic, not exhaustive)
     * ------------------------------------------------------- */
    private static final Set<String> KEYWORDS = Set.of(
            "abstract","assert","break","case","catch","class","const","continue",
            "default","do","else","enum","extends","final","finally","for","goto",
            "if","implements","import","instanceof","interface","native","new",
            "package","private","protected","public","return","strictfp","static",
            "super","switch","synchronized","this","throw","throws","transient",
            "try","volatile","while","record","sealed","permits","var"
    );

    private static final Set<String> TYPES = Set.of(
            "void","int","long","double","float","short","byte","char","boolean",
            "String","Object","List","Map","Set"
    );

    /* -------------------------------------------------------
     * Internal palette model (unchanged for built-ins)
     * ------------------------------------------------------- */
    private record Palette(String bg, String base, String kw, String type, String str,
                           String comment, String num, String annotation, String ch) {}

    private static final Map<String,Palette> PALETTES;
    static {
        Map<String,Palette> m = new LinkedHashMap<>();
        // name -> (bg, base, keyword, type, string, comment, number, annotation, char)
        m.put("default", new Palette("#ffffff","#222","#0000aa","#0044aa",
                "#aa1111","#777777","#aa00aa","#aa5500","#aa1111"));
        m.put("dark",    new Palette("#1e1e1e","#d4d4d4","#569cd6","#4fc1ff",
                "#ce9178","#6a9955","#b5cea8","#c586c0","#ce9178"));
        m.put("tango",   new Palette("#f8f8f8","#222","#204a87","#204a87",
                "#c41a16","#8f5902","#5c3566","#75507b","#c41a16"));
        PALETTES = Collections.unmodifiableMap(m);
    }

    /**
     * Returns ONLY the built-in palette names (external themes are listed
     * separately via ThemeLoader.listAvailableThemeNames()).
     */
    public static String[] availableThemes() {
        return PALETTES.keySet().toArray(new String[0]);
    }

    /* -------------------------------------------------------
     * Public API
     * ------------------------------------------------------- */

    /**
     * Highlight given code using themeName:
     *  - Try external JSON theme first
     *  - Fallback to internal palette
     */
    public static String highlight(String code, String themeName) {
        if (code == null || code.isBlank()) return "";
        Theme externalTheme = ThemeLoader.load(themeName); // may be null
        Palette palette = PALETTES.getOrDefault(themeName, PALETTES.get("default"));
        return doHighlight(code, externalTheme, palette);
    }

    /* -------------------------------------------------------
     * Core highlighting logic
     * ------------------------------------------------------- */
    private static String doHighlight(String code, Theme ext, Palette pal) {
        StringBuilder out = new StringBuilder(code.length() + 256);
        char[] chars = code.toCharArray();
        int i = 0;

        while (i < chars.length) {
            char c = chars[i];

            // Line comment //
            if (c == '/' && i + 1 < chars.length && chars[i + 1] == '/') {
                int start = i; i += 2;
                while (i < chars.length && chars[i] != '\n') i++;
                appendStyled(out, code.substring(start, i), "comment", ext, pal);
                continue;
            }

            // Block comment /* ... */
            if (c == '/' && i + 1 < chars.length && chars[i + 1] == '*') {
                int start = i; i += 2;
                while (i + 1 < chars.length && !(chars[i] == '*' && chars[i + 1] == '/')) i++;
                if (i + 1 < chars.length) i += 2; // consume */
                appendStyled(out, code.substring(start, i), "comment", ext, pal);
                continue;
            }

            // String literal
            if (c == '"') {
                int start = i++; boolean esc = false;
                while (i < chars.length) {
                    char d = chars[i++];
                    if (d == '\\' && !esc) { esc = true; continue; }
                    if (d == '"' && !esc) break;
                    esc = false;
                }
                appendStyled(out, code.substring(start, i), "string", ext, pal);
                continue;
            }

            // Char literal
            if (c == '\'') {
                int start = i++; boolean esc = false;
                while (i < chars.length) {
                    char d = chars[i++];
                    if (d == '\\' && !esc) { esc = true; continue; }
                    if (d == '\'' && !esc) break;
                    esc = false;
                }
                appendStyled(out, code.substring(start, i), "char", ext, pal);
                continue;
            }

            // Annotation (@Something)
            if (c == '@') {
                int start = i++;
                while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) i++;
                appendStyled(out, code.substring(start, i), "annotation", ext, pal);
                continue;
            }

            // Number
            if (Character.isDigit(c)) {
                int start = i++;
                while (i < chars.length &&
                        (Character.isDigit(chars[i]) ||
                         chars[i]=='.' || chars[i]=='_' ||
                         chars[i]=='x' || chars[i]=='X' ||
                         chars[i]=='b' || chars[i]=='B')) {
                    i++;
                }
                appendStyled(out, code.substring(start, i), "number", ext, pal);
                continue;
            }

            // Identifier / keyword / type
            if (Character.isJavaIdentifierStart(c)) {
                int start = i++;
                while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) i++;
                String token = code.substring(start, i);
                if (KEYWORDS.contains(token)) {
                    appendStyled(out, token, "keyword", ext, pal);
                } else if (TYPES.contains(token)) {
                    appendStyled(out, token, "type", ext, pal);
                } else {
                    appendStyled(out, token, "ident", ext, pal);
                }
                continue;
            }

            // Fallback: single character (punctuation / whitespace)
            escapeAppend(out, String.valueOf(c));
            i++;
        }

        String bg = pickBackground(ext, pal);
        return "<pre style=\"background:"+ bg +";padding:0.8rem;border:1px solid #ccc;overflow:auto;"
                + "font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;\">"
                + out + "</pre>";
    }

    /* -------------------------------------------------------
     * Styling helpers
     * ------------------------------------------------------- */

    private static void appendStyled(StringBuilder out,
                                     String raw,
                                     String kind,
                                     Theme ext,
                                     Palette pal) {
        String style = resolveStyle(kind, ext, pal);
        out.append("<span style=\"").append(style).append("\">");
        escapeAppend(out, raw);
        out.append("</span>");
    }

    private static String resolveStyle(String kind, Theme ext, Palette pal) {
        // External theme precedence
        if (ext != null) {
            String s = styleFromExternal(ext, kind);
            if (s != null) {
                return s;
            }
        }
        // Internal palette fallback
        if (pal != null) {
            return switch (kind) {
                case "keyword" -> "color:" + pal.kw + ";font-weight:bold;";
                case "type" -> "color:" + pal.type + ";font-weight:bold;";
                case "string" -> "color:" + pal.str + ";";
                case "char" -> "color:" + pal.ch + ";";
                case "comment" -> "color:" + pal.comment + ";font-style:italic;";
                case "number" -> "color:" + pal.num + ";";
                case "annotation" -> "color:" + pal.annotation + ";";
                case "ident" -> "color:" + pal.base + ";";
                default -> "color:" + pal.base + ";";
            };
        }
        // Absolute last resort
        return "color:#222;";
    }

    private static String styleFromExternal(Theme ext, String kind) {
        // Direct token style
        String direct = ext.getStyles().get(kind);
        if (direct != null && !direct.isBlank()) {
            return ensureColor(direct, ext);
        }
        // If missing, fallback to 'default'
        String def = ext.getStyles().get("default");
        if (def != null && !def.isBlank()) {
            return ensureColor(def, ext);
        }
        // If still missing, fallback to foreground
        if (ext.getForeground() != null) {
            return "color:" + ext.getForeground() + ";";
        }
        return null;
    }

    private static String ensureColor(String style, Theme ext) {
        if (style.contains("color:")) {
            return style;
        }
        if (ext.getForeground() != null) {
            return "color:" + ext.getForeground() + ";" + style;
        }
        return "color:#222;" + style;
    }

    private static String pickBackground(Theme ext, Palette pal) {
        if (ext != null && ext.getBackground() != null && !ext.getBackground().isBlank()) {
            return ext.getBackground();
        }
        if (pal != null) return pal.bg;
        return "#ffffff";
    }

    private static void escapeAppend(StringBuilder out, String s) {
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
    }
}