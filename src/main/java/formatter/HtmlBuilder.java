package formatter;

public class HtmlBuilder {
    public static String build(String assignment, String code, String output) {
        return "\n<h2>Assignment</h2>"
             + "\n<p>" + assignment + "</p>"
             + "\n<h2>My Code</h2>"
             + "\n" + code
             + "\n<h2>Output</h2>"
             + "\n<pre>" + output + "</pre>"
             + "\n<h2>Explanation</h2>"
             + "\n<p>[TODO: Write your explanation here]</p>";
    }
}
