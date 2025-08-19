package discussion_formatter;

public class Highlighter {
    private static String span(String style, String text) {
        return "<span style='" + style + "'>" + text + "</span>";
    }
    private static String[] colors(String theme) {
        if ("monokai".equalsIgnoreCase(theme)) {
            return new String[] {"#f92672;font-weight:bold;", "#e6db74;", "#75715e;", "#ae81ff;", "#66d9ef;font-weight:bold;"};
        } else if ("rrt".equalsIgnoreCase(theme)) {
            return new String[] {"#0000ff;font-weight:bold;", "#008000;", "#808080;", "#ff00ff;", "#2b91af;font-weight:bold;"};
        } else {
            return new String[] {"#204a87;font-weight:bold;", "#4e9a06;", "#888a85;", "#ad7fa8;", "#3465a4;font-weight:bold;"};
        }
    }
    public static String highlightJava(String source, String theme) {
        if (source == null) source = "";
        String code = source.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        String[] c = colors(theme);
        String kw=c[0], str=c[1], com=c[2], num=c[3], typ=c[4];
        // Comments
        code = code.replaceAll("(?s)/\\*.*?\\*/", m -> span(com, m.group()));
        code = code.replaceAll("(?m)//.*$", m -> span(com, m.group()));
        // Strings & chars
        code = code.replaceAll("\"([^\"\\\\]|\\\\.)*\"", m -> span(str, m.group()));
        code = code.replaceAll("'([^'\\\\]|\\\\.)*'", m -> span(str, m.group()));
        // Numbers
        code = code.replaceAll("\\b(0x[0-9a-fA-F]+|\\d+(?:_\\d+)*(?:\\.\\d+(?:_\\d+)*)?)\\b", m -> span(num, m.group()));
        // Types
        String[] types = {"boolean","byte","char","double","float","int","long","short","String","var"};
        for (String t: types) code = code.replaceAll("\\b"+t+"\\b", span(typ, t));
        // Keywords
        String[] keywords = {"abstract","assert","break","case","catch","class","const","continue","default","do","else","enum","extends","final","finally","for","goto","if","implements","import","instanceof","interface","native","new","package","private","protected","public","return","static","strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while"};
        for (String k: keywords) code = code.replaceAll("\\b"+k+"\\b", span(kw, k));
        return "<div style='margin-top:1em;'><pre><code>" + code + "</code></pre></div>";
    }
}
