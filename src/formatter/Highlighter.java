package formatter;

public class Highlighter {
    public static String highlight(String code, String style) {
        // Escape HTML entities
        code = code.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");

        // Very basic keyword coloring (expand later!)
        String[] keywords = {"class", "public", "static", "void", "int", "String"};
        for (String kw : keywords) {
            code = code.replaceAll("\b" + kw + "\b",
                    "<span style='color: #204a87; font-weight: bold;'>" + kw + "</span>");
        }

        return "<pre style='background:#f8f8f8; padding:10px;'>" + code + "</pre>";
    }
}
