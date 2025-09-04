package formatter;

import java.util.*;
//import java.util.regex.Pattern; 

/**
 * Very lightweight Java-like syntax highlighter.
 * Produces inline-styled HTML with <span style="..."> tokens.
 * Uses internal palettes keyed by theme name. (Later: integrate ThemeLoader.)
 */
public final class Highlighter {

    private Highlighter() {}

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

    private record Palette(String bg, String base, String kw, String type, String str,
                           String comment, String num) {}

    private static final Map<String,Palette> PALETTES;
    static {
        Map<String,Palette> m = new LinkedHashMap<>();
        m.put("default", new Palette("#ffffff","#222","#0000aa","#0044aa",
                "#aa1111","#777777","#aa00aa"));
        m.put("dark",    new Palette("#1e1e1e","#d4d4d4","#569cd6","#4fc1ff",
                "#ce9178","#6a9955","#b5cea8"));
        m.put("tango",   new Palette("#f8f8f8","#222","#204a87","#204a87",
                "#c41a16","#8f5902","#5c3566"));
        PALETTES = Collections.unmodifiableMap(m);
    }

    public static String[] availableThemes() {
        return PALETTES.keySet().toArray(new String[0]);
    }

    public static String highlight(String code, String themeName) {
        if (code == null || code.isBlank()) return "";
        Palette p = PALETTES.getOrDefault(themeName, PALETTES.get("default"));

        StringBuilder out = new StringBuilder(code.length() + 256);
        // Simple state machine scanning
        char[] chars = code.toCharArray();
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];

            // Line comment //
            if (c == '/' && i + 1 < chars.length && chars[i+1] == '/') {
                int start = i; i += 2;
                while (i < chars.length && chars[i] != '\n') i++;
                appendSpan(out, code.substring(start, i), "color:"+p.comment+";font-style:italic;");
                continue;
            }
            // Block comment /* ... */
            if (c == '/' && i + 1 < chars.length && chars[i+1] == '*') {
                int start = i; i += 2;
                while (i + 1 < chars.length && !(chars[i] == '*' && chars[i+1] == '/')) i++;
                if (i + 1 < chars.length) i += 2; // consume */
                appendSpan(out, code.substring(start, i), "color:"+p.comment+";font-style:italic;");
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
                appendSpan(out, code.substring(start, i), "color:"+p.str+";");
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
                appendSpan(out, code.substring(start, i), "color:"+p.str+";");
                continue;
            }
            // Number
            if (Character.isDigit(c)) {
                int start = i++;
                while (i < chars.length && (Character.isDigit(chars[i]) || chars[i] == '.' || chars[i]=='_')) i++;
                appendSpan(out, code.substring(start, i), "color:"+p.num+";");
                continue;
            }
            // Identifier / keyword
            if (Character.isJavaIdentifierStart(c)) {
                int start = i++;
                while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) i++;
                String token = code.substring(start, i);
                if (KEYWORDS.contains(token)) {
                    appendSpan(out, token, "color:"+p.kw+";font-weight:bold;");
                } else if (TYPES.contains(token)) {
                    appendSpan(out, token, "color:"+p.type+";font-weight:bold;");
                } else {
                    escapeAppend(out, token);
                }
                continue;
            }
            // Fallback single char
            escapeAppend(out, String.valueOf(c));
            i++;
        }

        // Wrap in container pre
        return "<pre style=\"background:"+p.bg+";padding:0.8rem;border:1px solid #ccc;overflow:auto;"
                + "font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;\">"
                + out + "</pre>";
    }

    private static void appendSpan(StringBuilder out, String raw, String style) {
        out.append("<span style=\"").append(style).append("\">");
        escapeAppend(out, raw);
        out.append("</span>");
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