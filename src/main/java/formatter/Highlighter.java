package formatter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlighter {
    private static class Theme {
        String background;
        String text;
        String keyword;
        Theme(String bg, String tx, String kw) { this.background = bg; this.text = tx; this.keyword = kw; }
    }

    private static final Map<String, Theme> THEMES = new HashMap<>();
    static {
        // Basic palettes inspired by common themes
        THEMES.put("tango",   new Theme("#f8f8f8", "#2e3436", "#204a87"));
        THEMES.put("monokai", new Theme("#272822", "#f8f8f2", "#f92672"));
        THEMES.put("rrt",     new Theme("#1e1e1e", "#d4d4d4", "#569cd6"));
        THEMES.put("light",   new Theme("#ffffff", "#000000", "#0000aa"));
        THEMES.put("dark",    new Theme("#000000", "#e5e5e5", "#00ccff"));
    }

    private static Theme pickTheme(String name) {
        if (name == null) return THEMES.get("tango");
        Theme t = THEMES.get(name.toLowerCase());
        return t != null ? t : THEMES.get("tango");
    }

    public static String highlight(String code, String style) {
        Theme theme = pickTheme(style);

        // Escape HTML entities
        code = escapeHtml(code);

        // Basic keyword coloring without Java 9+ APIs (no lambda replaceAll)
        String[] keywords = {"class", "public", "static", "void", "int", "String", "extends", "implements", "new", "return"};
        for (String kw : keywords) {
            code = wrapWord(code, kw, "<span style='color: " + theme.keyword + "; font-weight: bold;'>" + kw + "</span>");
        }

        return "<pre style='background:" + theme.background + "; color:" + theme.text + "; padding:10px; overflow:auto;'>" 
             + code + "</pre>";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Replaces whole-word occurrences of `word` in `text` with `replacement` using Pattern/Matcher
    private static String wrapWord(String text, String word, String replacement) {
        Pattern p = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String[] availableThemes() {
        return THEMES.keySet().toArray(new String[0]);
    }
}
