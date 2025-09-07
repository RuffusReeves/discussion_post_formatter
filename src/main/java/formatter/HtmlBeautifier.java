package formatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Attempts to "tidy" (pretty-print) HTML.
 *
 * Strategy:
 *  1. If config says tidy_html = false (case-insensitive), return original.
 *  2. Try external HTML Tidy (tidy or tidy.exe) if available on PATH.
 *  3. If external tidy fails/not found, fallback to a naive internal formatter.
 *  4. If all formatting attempts fail, return original.
 *
 * Optional debug:
 *  - If config tidy_debug = true, include Tidy stderr warnings as an HTML comment
 *    (when tidy succeeds) or print them to stdout (when tidy fails).
 */
public final class HtmlBeautifier {

    private HtmlBeautifier() {}

    public static String maybeBeautify(Config config, String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) return rawHtml;

        String flag = config.get("tidy_html");
        if (flag != null && flag.trim().equalsIgnoreCase("false")) {
            return rawHtml;
        }

        boolean debug = false;
        String dbg = config.get("tidy_debug");
        if (dbg != null && dbg.trim().equalsIgnoreCase("true")) {
            debug = true;
        }

        // Try external Tidy first
        try {
            String tidyPath = findTidyExecutable();
            if (tidyPath != null) {
                if (debug) {
                    System.out.println("[HtmlBeautifier] Using external tidy: " + tidyPath);
                }
                TidyResult tr = runExternalTidy(tidyPath, rawHtml);
                if (tr != null && tr.output() != null && !tr.output().isBlank()) {
                    String output = tr.output();
                    // Collapse newlines before selected closers, pre-aware
                    output = collapseNewlinesBeforeClosersPreAware(output);
                    if (debug && tr.stderr() != null && !tr.stderr().isBlank()) {
                        return buildDebugComment(tr.stderr()) + output;
                    }
                    return output;
                } else if (debug && tr != null && tr.stderr() != null && !tr.stderr().isBlank()) {
                    System.out.println("[HtmlBeautifier] External tidy failed output; stderr:\n" + tr.stderr());
                }
            } else if (debug) {
                System.out.println("[HtmlBeautifier] No tidy executable found on PATH.");
            }
        } catch (Exception e) {
            if (debug) {
                System.out.println("[HtmlBeautifier] External tidy attempt threw: " + e.getMessage());
            }
        }

        // Fallback: pre-aware internal formatting
        try {
            String pretty = naivePrettyPrint(rawHtml);
            // Collapse newlines before selected closers, pre-aware
            pretty = collapseNewlinesBeforeClosersPreAware(pretty);
            if (debug) {
                System.out.println("[HtmlBeautifier] Used naive formatter fallback (pre-aware).");
            }
            return pretty;
        } catch (Exception e) {
            if (debug) {
                System.out.println("[HtmlBeautifier] Naive formatter failed: " + e.getMessage());
            }
            return rawHtml; // ultimate fallback
        }
    }

    /* -------------------------------------------------------
       External Tidy support
       ------------------------------------------------------- */

    private static String findTidyExecutable() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String[] candidates = os.contains("win")
                ? new String[]{"tidy.exe", "tidy"}
                : new String[]{"tidy"};
        for (String c : candidates) {
            if (executableOnPath(c)) {
                return c;
            }
        }
        return null;
    }

    private static boolean executableOnPath(String exe) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        String[] parts = path.split(File.pathSeparator);
        for (String dir : parts) {
            File f = new File(dir, exe);
            if (f.isFile() && f.canExecute()) {
                return true;
            }
        }
        return false;
    }

    private record TidyResult(String output, String stderr, int exitCode) {}

    private static TidyResult runExternalTidy(String tidyCmd, String inputHtml) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                tidyCmd,
                "-quiet",
                "-indent",
                "-wrap", "120",
                "--indent-spaces", "2",
                "--vertical-space", "yes",
                "--tidy-mark", "no",
                "--drop-empty-elements", "no",
                "--preserve-entities", "yes",
                // Ensure HTML5 parsing so modern sectioning tags are preserved
                "--doctype", "html5",
                // Be explicit about HTML5 sectioning elements as block-level
                "--new-blocklevel-tags", "main,section,article,header,footer,nav"
        );
        Process proc = pb.start();
        try (OutputStream os = proc.getOutputStream()) {
            os.write(inputHtml.getBytes(StandardCharsets.UTF_8));
        }

        String stdout = readAll(proc.getInputStream());
        String stderr = readAll(proc.getErrorStream());
        int exit = proc.waitFor();

        if ((exit == 0 || exit == 1) && stdout != null && !stdout.isBlank()) {
            String cleaned = stdout.endsWith("\n") ? stdout : stdout + "\n";
            return new TidyResult(cleaned, stderr, exit);
        }
        return new TidyResult(null, stderr, exit);
    }

    private static String readAll(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (!first) sb.append('\n');
                sb.append(line);
                first = false;
            }
            return sb.toString();
        }
    }

    private static String buildDebugComment(String stderr) {
        String sanitized = stderr.replace("-->", "--&gt;");
        return "<!-- HTML Tidy Warnings/Errors:\n" + sanitized + "\n-->\n";
    }

    /* -------------------------------------------------------
       Pre-aware naive formatter
       ------------------------------------------------------- */

    private static String naivePrettyPrint(String html) {
        // Normalize newlines for consistency
        String normalized = html.replace("\r\n", "\n").replace("\r", "\n");

        StringBuilder out = new StringBuilder(normalized.length() + 256);
        int pos = 0;
        while (pos < normalized.length()) {
            int preOpen = indexOfIgnoreCase(normalized, "<pre", pos);
            if (preOpen < 0) {
                // No more <pre>; pretty-print the rest (outside-pre)
                prettyOutside(out, normalized.substring(pos));
                break;
            }
            // Pretty-print segment before <pre>
            prettyOutside(out, normalized.substring(pos, preOpen));

            // Find end of the <pre ...> open tag
            int preTagEnd = normalized.indexOf('>', preOpen);
            if (preTagEnd < 0) {
                // Malformed; just append the rest
                out.append(normalized.substring(preOpen));
                break;
            }

            // Find matching closing </pre>
            int preClose = indexOfIgnoreCase(normalized, "</pre>", preTagEnd + 1);
            if (preClose < 0) {
                // No closing; append the rest unchanged
                out.append(normalized.substring(preOpen));
                break;
            }

            // Pass the entire <pre>...</pre> block through unchanged
            int preEnd = preClose + 6; // "</pre>".length()
            out.append(normalized, preOpen, preEnd);
            pos = preEnd;
        }
        return out.toString();
    }

    // Pretty-print a fragment that is guaranteed to be outside any <pre>...</pre>
    private static void prettyOutside(StringBuilder out, String fragment) {
        int indent = 0;
        final int n = fragment.length();
        StringBuilder token = new StringBuilder();
        boolean inTag = false;

        for (int i = 0; i < n; i++) {
            char c = fragment.charAt(i);
            if (!inTag) {
                if (c == '<') {
                    // flush text token
                    if (token.length() > 0) {
                        String text = token.toString().trim();
                        if (!text.isEmpty()) {
                            appendIndent(out, indent).append(text).append('\n');
                        }
                        token.setLength(0);
                    }
                    inTag = true;
                    token.append(c);
                } else {
                    token.append(c);
                }
            } else {
                token.append(c);
                if (c == '>') {
                    String tag = token.toString();
                    token.setLength(0);
                    inTag = false;

                    String tLower = tag.toLowerCase();
                    if (tLower.startsWith("<!--")) {
                        // Treat comments as a single, non-indenting line
                        appendIndent(out, indent).append(tag.trim()).append('\n');
                        continue;
                    }

                    String name = extractTagName(tLower);
                    boolean closing = tLower.startsWith("</");
                    boolean selfClosing = tLower.endsWith("/>") || isVoidElement(name);

                    if (closing) {
                        if (indent > 0) indent--;
                        appendIndent(out, indent).append(tag.trim()).append('\n');
                    } else {
                        appendIndent(out, indent).append(tag.trim()).append('\n');
                        if (!selfClosing) {
                            indent++;
                        }
                    }
                }
            }
        }

        // Flush any trailing text token
        if (token.length() > 0) {
            String text = token.toString().trim();
            if (!text.isEmpty()) {
                appendIndent(out, indent).append(text).append('\n');
            }
        }
    }

    private static String extractTagName(String tagLower) {
        // Expects something like "<tag ...>" or "</tag ...>"
        int start = 1; // skip '<'
        if (tagLower.length() <= 1) return "";
        if (tagLower.charAt(1) == '/') start = 2;
        int i = start;
        while (i < tagLower.length()) {
            char ch = tagLower.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != ':' && ch != '-' && ch != '_') break;
            i++;
        }
        return (i > start) ? tagLower.substring(start, i) : "";
    }

    private static boolean isVoidElement(String name) {
        // HTML void elements: no separate closing tag and not container-like
        return "area".equals(name) || "base".equals(name) || "br".equals(name)
                || "col".equals(name) || "embed".equals(name) || "hr".equals(name)
                || "img".equals(name) || "input".equals(name) || "link".equals(name)
                || "meta".equals(name) || "param".equals(name) || "source".equals(name)
                || "track".equals(name) || "wbr".equals(name);
    }

    private static int indexOfIgnoreCase(String haystack, String needle, int fromIndex) {
        int hLen = haystack.length();
        int nLen = needle.length();
        if (nLen == 0) return fromIndex <= hLen ? fromIndex : -1;

        char firstLower = Character.toLowerCase(needle.charAt(0));
        char firstUpper = Character.toUpperCase(needle.charAt(0));

        for (int i = Math.max(0, fromIndex); i <= hLen - nLen; i++) {
            char c = haystack.charAt(i);
            if (c != firstLower && c != firstUpper) continue;
            if (haystack.regionMatches(true, i, needle, 0, nLen)) {
                return i;
            }
        }
        return -1;
    }

    private static StringBuilder appendIndent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
        return sb;
    }

    /* -------------------------------------------------------
       Post-processing: collapse newline before certain closers (pre-aware)
       ------------------------------------------------------- */

    // Collapse any optional indentation + newline immediately before </span>, </p>, or </code>,
    // but do not touch content inside <pre> blocks.
    private static String collapseNewlinesBeforeClosersPreAware(String html) {
        StringBuilder out = new StringBuilder(html.length());
        int pos = 0;
        while (pos < html.length()) {
            int preOpen = indexOfIgnoreCase(html, "<pre", pos);
            if (preOpen < 0) {
                out.append(collapseNewlinesBeforeClosersSimple(html.substring(pos)));
                break;
            }
            out.append(collapseNewlinesBeforeClosersSimple(html.substring(pos, preOpen)));

            int preTagEnd = html.indexOf('>', preOpen);
            if (preTagEnd < 0) {
                out.append(html.substring(preOpen));
                break;
            }
            int preClose = indexOfIgnoreCase(html, "</pre>", preTagEnd + 1);
            if (preClose < 0) {
                out.append(html.substring(preOpen));
                break;
            }
            int preEnd = preClose + 6; // length of "</pre>"
            out.append(html, preOpen, preEnd);
            pos = preEnd;
        }
        return out.toString();
    }

    // Perform the collapse on a fragment that is guaranteed to be outside <pre>
    private static String collapseNewlinesBeforeClosersSimple(String frag) {
        // (?m) multiline, \R any line break; remove indentation+newline before the closing tag
        return frag.replaceAll("(?m)[ \\t]*\\R[ \\t]*(</(?:span|p|code)>)", "$1");
    }
}