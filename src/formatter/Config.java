package formatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public String assignmentFile;
    public String codeFile;
    public String outputFile;
    public String theme;

    public static Config load(String path, String unitNumber) throws IOException {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            p.load(fis);
        }

        Config c = new Config();
        c.assignmentFile = replaceUnitNumber(p.getProperty("assignment_file", "assignment.txt"), unitNumber);
        c.codeFile = replaceUnitNumber(p.getProperty("code_file", "Begin.java"), unitNumber);
        c.outputFile = replaceUnitNumber(p.getProperty("output_file", "DiscussionPost.html"), unitNumber);
        c.theme = p.getProperty("theme", "tango");
        return c;
    }

    private static String replaceUnitNumber(String text, String unitNumber) {
        if (text == null) return null;
        return text.replace("<UNIT_NUMBER>", unitNumber);
    }
}
