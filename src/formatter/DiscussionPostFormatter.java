package formatter;

public class DiscussionPostFormatter {
    public static void main(String[] args) throws Exception {
        // Load config
        Config config = Config.load("config.txt");

        // Read assignment
        String assignment = Utils.readFile(config.assignmentText);

        // Read + highlight code
        String code = Utils.readFile(config.codeFile);
        String highlightedCode = Highlighter.highlight(code, config.style);

        // Run code and capture output
        String output = Utils.runJavaFile(config.codeFile);

        // Build HTML
        String html = HtmlBuilder.build(assignment, highlightedCode, output);

        // Save HTML
        Utils.writeFile(config.outputHtml, html);

        System.out.println("Discussion post created at: " + config.outputHtml);
    }
}
