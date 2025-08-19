package discussion_formatter;

import java.io.*;
import java.util.*;

public class Config {
    public String assignmentFile;
    public String codeFile;
    public String outputFile;
    public String theme;
    public static Config load(String path) throws IOException {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) { p.load(fis); }
        Config c = new Config();
        c.assignmentFile = p.getProperty("assignment_file", "assignment.txt");
        c.codeFile = p.getProperty("code_file", "Begin.java");
        c.outputFile = p.getProperty("output_file", "DiscussionPost.html");
        c.theme = p.getProperty("theme", "tango");
        return c;
    }
}
