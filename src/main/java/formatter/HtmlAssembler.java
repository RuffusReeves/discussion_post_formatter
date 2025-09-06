// Current filename: HtmlAssembler.java

package formatter;

import java.util.List;

/**
 * HtmlAssembler (Inline-Style Only, No class attributes, No <style> tag)
 */
public class HtmlAssembler {

    private static final String BODY_STYLE =
            "font-family:Arial,sans-serif; margin:2rem; line-height:1.45;";
    private static final String MAIN_STYLE =
            "display:block; width:100%; max-width:960px; margin:0 auto;";
    private static final String SECTION_STYLE = "margin-bottom:2rem;";
    private static final String H2_STYLE = "margin:0 0 0.75rem 0; font-size:1.4rem; line-height:1.2;";
    private static final String DIV_TEXT_STYLE = "line-height:1.45;";
    private static final String PRE_OUTPUT_STYLE =
            "background:#f5f5f5; padding:1em; border:1px solid #ccc; overflow:auto; " +
            "font-family:'Courier New',monospace; font-size:0.95rem; line-height:1.3;";
    private static final String PRE_CODEBLOCK_FALLBACK_STYLE =
            "background:#f5f5f5; padding:1em; border:1px solid #ccc; overflow:auto;";
    private static final String INLINE_CODE_STYLE =
            "background:#f1f1f1; padding:2px 4px; border-radius:3px; " +
            "font-family:'Courier New',monospace; font-size:0.95em;";
    private static final String EXPLANATION_BOX_STYLE =
            "background:#fff9e6; padding:1em; border:1px solid #eedc82;";
    private static final String FOOTER_STYLE =
            "margin-top:3rem; font-size:0.7rem; color:#555; opacity:0.85; text-align:center;";

    public static String assemble(List<ContentBlock> blocks) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">")
          .append("\n<head>")
          .append("\n\t<meta charset=\"UTF-8\" />")
          .append("\n\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
          .append("\n\t<title>Discussion Post</title>")
          .append("\n\t<meta name=\"author\" content=\"Joe Reeves\" />")
          .append("\n</head>")
          .append("\n\n<body style=\"").append(BODY_STYLE).append("\">")
          .append("\n\t<main style=\"").append(MAIN_STYLE).append("\">");

        for (ContentBlock block : blocks) {
            appendBlock(sb, block);
        }

        sb.append("\n\t<footer style=\"").append(FOOTER_STYLE).append("\">")
          .append("<!-- Formatted by Jemz using Discussion Post Formatter -->")
          .append(" | Created by RuffusReeves with the assistance of Github Copilot using GPT-5")
          .append("\n\t</footer>")
          .append("\n\t</main>")
          .append("\n\n</body>")
          .append("\n</html>");

        return sb.toString();
    }

    private static void appendBlock(StringBuilder sb, ContentBlock block) {
        switch (block.getType()) {
            case ASSIGNMENT_TEXT -> {
                sb.append("<section style=\"").append(SECTION_STYLE).append("\">");
                sb.append("<h2 style=\"").append(H2_STYLE).append("\">Assignment</h2>");
                sb.append("<div style=\"").append(DIV_TEXT_STYLE).append("\">")
                  .append(escapeHtml(block.getContent()))
                  .append("</div>");
                sb.append("</section>\n");
            }
            case HIGHLIGHTED_CODE -> {
                sb.append("<section style=\"").append(SECTION_STYLE).append("\">")
                  .append("<h2 style=\"").append(H2_STYLE).append("\">Code</h2>");
                if (containsPre(block.getContent())) {
                    sb.append(block.getContent());
                } else {
                    sb.append("<pre style=\"").append(PRE_CODEBLOCK_FALLBACK_STYLE).append("\">")
                      .append(escapeHtml(block.getContent()))
                      .append("</pre>");
                }
                sb.append("</section>\n");
            }
            case PROGRAM_OUTPUT -> {
                sb.append("<section style=\"").append(SECTION_STYLE).append("\">")
                  .append("<h2 style=\"").append(H2_STYLE).append("\">Output</h2>")
                  .append("<pre style=\"").append(PRE_OUTPUT_STYLE).append("\">")
                  .append(escapeHtml(block.getContent()))
                  .append("</pre>")
                  .append("</section>\n");
            }
            case EXPLANATION_PLACEHOLDER -> {
                sb.append("<section style=\"").append(SECTION_STYLE).append("\">")
                  .append("<h2 style=\"").append(H2_STYLE).append("\">Explanation</h2>")
                  .append("<div style=\"").append(EXPLANATION_BOX_STYLE).append("\">")
                  .append("<p style=\"margin:0;\">[TODO: Write your explanation here]</p>")
                  .append("</div>")
                  .append("</section>\n");
            }
            case SECTION_HEADER -> {
                sb.append("<section style=\"").append(SECTION_STYLE).append("\">")
                  .append("<h2 style=\"").append(H2_STYLE).append("\">")
                  .append(escapeHtml(block.getContent()))
                  .append("</h2>")
                  .append("</section>\n");
            }
            case INLINE_CODE -> {
                sb.append("<code style=\"").append(INLINE_CODE_STYLE).append("\">")
                  .append(escapeHtml(block.getContent()))
                  .append("</code>\n");
            }
            default -> sb.append("<!-- Unknown block type: ").append(block.getType()).append(" -->\n");
        }
    }

    private static boolean containsPre(String html) {
        if (html == null) return false;
        return html.toLowerCase().contains("<pre");
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