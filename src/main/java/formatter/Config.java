package formatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration management for the Discussion Post Formatter.
 * 
 * This class handles loading configuration from properties files and provides
 * placeholder replacement functionality for dynamic file paths based on unit
 * numbers and other variables.
 * 
 * TODO: Add configuration validation and error messages
 * TODO: Implement configuration caching for performance
 * TODO: Add support for environment variable substitution
 * TODO: Create configuration builder pattern for complex setups
 */
public class Config {
    public String assignmentFile;
    public String codeFile;
    public String outputFile;
    public String theme;
    
    /**
     * Loads configuration from a properties file with placeholder replacement.
     * 
     * Supports the following placeholders:
     * - <UNIT_NUMBER>: Replaced with the provided unit number
     * 
     * @param path Path to the configuration file
     * @param unitNumber Unit number for placeholder replacement
     * @return Configured Config object
     * @throws IOException If configuration file cannot be read
     * 
     * TODO: Add more placeholder types (date, user, etc.)
     * TODO: Implement configuration validation
     * TODO: Add default configuration fallbacks
     * TODO: Support multiple configuration file formats (YAML, JSON)
     */
    public static Config load(String path, String unitNumber) throws IOException {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            p.load(fis);
        }

        Config c = new Config();
        c.assignmentFile = replaceUnitNumber(p.getProperty("assignment_file", "assignment.txt"), unitNumber);
        c.codeFile = replaceUnitNumber(p.getProperty("code_file", "Begin.java"), unitNumber);
        c.outputFile = replaceUnitNumber(p.getProperty("output_file", "DiscussionPost.html"), unitNumber);
        c.theme = p.getProperty("theme", "default");
        
        // TODO: Validate configuration values
        // TODO: Check if files exist and provide helpful error messages
        // TODO: Add logging for configuration loading
        
        return c;
    }

    /**
     * Replaces unit number placeholders in configuration values.
     * 
     * @param text Text containing placeholders
     * @param unitNumber Unit number to substitute
     * @return Text with placeholders replaced
     * 
     * TODO: Support additional placeholder types
     * TODO: Add placeholder validation and error handling
     * TODO: Implement recursive placeholder resolution
     */
    private static String replaceUnitNumber(String text, String unitNumber) {
        if (text == null) return null;
        return text.replace("<UNIT_NUMBER>", unitNumber);
    }
    
    /**
     * Validates the current configuration.
     * 
     * @return true if configuration is valid, false otherwise
     * 
     * TODO: Implement configuration validation
     * TODO: Add specific validation rules for each property
     * TODO: Provide detailed validation error messages
     */
    public boolean isValid() {
        // TODO: Implement validation logic
        // Check file paths, theme validity, etc.
        return true; // Placeholder
    }
    
    /**
     * Returns a string representation of the configuration for debugging.
     * 
     * @return Configuration details as string
     * 
     * TODO: Add option to mask sensitive information
     */
    @Override
    public String toString() {
        return "Config{" +
                "assignmentFile='" + assignmentFile + '\'' +
                ", codeFile='" + codeFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
