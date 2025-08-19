package discussion_formatter;

public class HtmlBuilder {
    public static String build(String assignment, String highlightedCodeBlock, String output) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='margin-top:1em;'><p>")
            .append(escape(assignment).replace("\n", "<br/>"))
            .append("</p></div>\n");
        html.append(highlightedCodeBlock).append("\n");
        html.append("<div style='margin-top:1em;'><pre><code>")
            .append(escape(output))
            .append("</code></pre></div>\n");
        html.append("<div style='margin-top:1em;'><p>[Your explanation goes here]</p></div>\n");
        return html.toString();
    }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
