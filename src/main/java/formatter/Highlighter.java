package formatter;

import java.util.*;

/**
 * Lightweight inline-style syntax highlighter producing:
 *   <pre style="..."> ... <span style="...">token</span> ... </pre>
 *
 * Constraints:
 *  - No class attributes (only inline style)
 *  - No <style> block
 */
public class Highlighter {

    private static class Theme {
        final String background;
        final String text;
        final String keyword;
        final String string;
        final String comment;
        final String number;
        final String type;

        Theme(String bg, String tx, String kw, String str, String cm, String num, String typeColor) {
            this.background = bg;
            this.text = tx;
            this.keyword = kw;
            this.string = str;
            this.comment = cm;
            this.number = num;
            this.type = typeColor;
        }
    }

    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();
    static {
        THEMES.put("tango",   new Theme("#f8f8f8", "#2e3436", "#204a87", "#c41a16", "#8f5902", "#1c00cf", "#795da3"));
        THEMES.put("monokai", new Theme("#272822", "#f8f8f2", "#f92672", "#e6db74", "#75715e", "#ae81ff", "#66d9ef"));
        THEMES.put("dark",    new Theme("#000000", "#e5e5e5", "#00ccff", "#ffcc66", "#888888", "#99cc99", "#ff99ff"));
        THEMES.put("light",   new Theme("#ffffff", "#000000", "#0000aa", "#a31515", "#008000", "#09885a", "#7b4ca3"));
        THEMES.put("default", THEMES.get("tango"));
    }

    private static Theme pickTheme(String name) {
        if (name == null) return THEMES.get("default");
        return THEMES.getOrDefault(name.toLowerCase(), THEMES.get("default"));
    }

    public static String[] availableThemes() {
        return THEMES.keySet().toArray(new String[0]);
    }

    private static final Set<String> KEYWORDS = Set.of(
            "public","private","protected","static","final","abstract","class","interface","enum",
            "if","else","switch","case","default","for","while","do","return","new","try","catch","finally",
            "throw","throws","extends","implements","import","package","this","super","void","break","continue",
            "true","false","null","instanceof","var","record"
    );

    private static final Set<String> TYPE_LIKE = Set.of(
            "String","Integer","Long","Double","Float","Short","Byte","Boolean","Character",
            "List","Map","Set","ArrayList","HashMap","HashSet"
    );

    private enum TokenType { COMMENT, STRING, NUMBER, KEYWORD, TYPE, IDENT, SYMBOL, WHITESPACE }

    private static class Token {
        final TokenType type;
        final String text;
        Token(TokenType t, String txt) { this.type = t; this.text = txt; }
    }

    public static String highlight(String code, String themeName) {
        Theme theme = pickTheme(themeName);
        if (code == null) code = "";
        String escaped = escapeHtml(code);
        var tokens = tokenize(escaped);

        StringBuilder out = new StringBuilder();
        out.append("<pre style=\"background:")
           .append(theme.background)
           .append(";color:")
           .append(theme.text)
           .append(";padding:10px;overflow:auto;font-family:'Courier New',monospace;font-size:0.9rem;line-height:1.3;\">");

        for (Token t : tokens) {
            switch (t.type) {
                case KEYWORD -> wrap(out, t.text, "color:" + theme.keyword + ";font-weight:bold;");
                case TYPE    -> wrap(out, t.text, "color:" + theme.type + ";font-weight:bold;");
                case STRING  -> wrap(out, t.text, "color:" + theme.string + ";");
                case COMMENT -> wrap(out, t.text, "color:" + theme.comment + ";font-style:italic;");
                case NUMBER  -> wrap(out, t.text, "color:" + theme.number + ";");
                default      -> out.append(t.text);
            }
        }

        out.append("</pre>");
        return out.toString();
    }

    private static List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);

            if (Character.isWhitespace(c)) {
                int start = i;
                while (i < n && Character.isWhitespace(s.charAt(i))) i++;
                tokens.add(new Token(TokenType.WHITESPACE, s.substring(start, i)));
                continue;
            }

            if (c == '/' && i + 1 < n && s.charAt(i+1) == '*') {
                int start = i; i += 2;
                while (i < n - 1 && !(s.charAt(i) == '*' && s.charAt(i+1) == '/')) i++;
                if (i < n - 1) i += 2;
                tokens.add(new Token(TokenType.COMMENT, s.substring(start, i)));
                continue;
            }

            if (c == '/' && i + 1 < n && s.charAt(i+1) == '/') {
                int start = i; i += 2;
                while (i < n && s.charAt(i) != '\n') i++;
                tokens.add(new Token(TokenType.COMMENT, s.substring(start, i)));
                continue;
            }

            if (c == '"') {
                int start = i; i++;
                while (i < n) {
                    char cc = s.charAt(i);
                    if (cc == '"') { i++; break; }
                    if (cc == '\\' && i + 1 < n) i += 2; else i++;
                }
                tokens.add(new Token(TokenType.STRING, s.substring(start, i)));
                continue;
            }

            if (Character.isDigit(c)) {
                int start = i;
                while (i < n && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) i++;
                tokens.add(new Token(TokenType.NUMBER, s.substring(start, i)));
                continue;
            }

            if (Character.isJavaIdentifierStart(c)) {
                int start = i; i++;
                while (i < n && Character.isJavaIdentifierPart(s.charAt(i))) i++;
                String ident = s.substring(start, i);
                if (KEYWORDS.contains(ident)) tokens.add(new Token(TokenType.KEYWORD, ident));
                else if (TYPE_LIKE.contains(ident)) tokens.add(new Token(TokenType.TYPE, ident));
                else tokens.add(new Token(TokenType.IDENT, ident));
                continue;
            }

            tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c)));
            i++;
        }
        return tokens;
    }

    private static void wrap(StringBuilder sb, String text, String style) {
        sb.append("<span style=\"").append(style).append("\">").append(text).append("</span>");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }
}