package discussion_formatter;

public class DiscussionPostFormatter {
    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.txt");
        String assignment = Utils.readFile(config.assignmentFile);
        String code = Utils.readFile(config.codeFile);
        String highlighted = Highlighter.highlightJava(code, config.theme);
        String output = Utils.runJavaFile(config.codeFile);
        String html = HtmlBuilder.build(assignment, highlighted, output);
        Utils.writeFile(config.outputFile, html);
        System.out.println("Discussion post created at: " + config.outputFile);
    }
}
