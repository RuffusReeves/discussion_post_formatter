package formatter;

import java.io.*;
import java.util.*;

public class Config {
    public String assignmentText;
    public String codeFile;
    public String outputHtml;
    public String style;

    public static Config load(String path) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(path));

        Config c = new Config();
        c.assignmentText = props.getProperty("assignment_text");
        c.codeFile = props.getProperty("code_file");
        c.outputHtml = props.getProperty("output_html");
        c.style = props.getProperty("style", "light");
        return c;
    }
}
