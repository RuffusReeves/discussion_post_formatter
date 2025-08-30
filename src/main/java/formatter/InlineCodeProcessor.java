package formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal inline code processor (still optional in your pipeline).
 */
public class InlineCodeProcessor {

    private static final Pattern BACKTICK_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern CODE_TAG_PATTERN = Pattern.compile("<code>([^<]+)</code>");
    private static final Pattern TRIPLE_BACKTICK_PATTERN = Pattern.compile("```([\\s\\S]*?)```");

    public static String process(String prose) {
        if (prose == null || prose.isEmpty()) return "";
        String result = processTripleBackticks(prose);
        result = processBackticks(result);
        result = processCodeTags(result);
        return result;
    }

    private static String processBackticks(String text) {
        Matcher m = BACKTICK_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = escapeHtml(m.group(1).trim());
            String replacement = "<code style=\"background:#f1f1f1;padding:2px 4px;border-radius:3px; font-family:'Courier New',monospace;\">" + code + "</code>";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String processCodeTags(String text) {
        Matcher m = CODE_TAG_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = escapeHtml(m.group(1).trim());
            String replacement = "<code style=\"background:#f1f1f1;padding:2px 4px;border-radius:3px; font-family:'Courier New',monospace;\">" + code + "</code>";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String processTripleBackticks(String text) {
        Matcher m = TRIPLE_BACKTICK_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = escapeHtml(stripTrailingNewline(m.group(1)));
            String replacement = "<pre style=\"background:#f5f5f5;padding:1em;border:1px solid #ccc;overflow:auto; font-family:'Courier New',monospace;\">" + code + "</pre>";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String stripTrailingNewline(String s) {
        if (s == null) return "";
        return s.endsWith("\n") ? s.substring(0, s.length() - 1) : s;
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