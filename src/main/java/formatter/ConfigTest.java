package formatter;

/**
 * Simple test class to verify the file content loading functionality.
 */
public class ConfigTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing Config file content loading functionality...\n");
        
        // Load the test config
        Config config = Config.load("test_config.txt");
        
        // Test 1: Verify derived keys are generated correctly
        System.out.println("=== Test 1: Checking derived key generation ===");
        
        String assignmentContents = config.get("assignmentTextFileContents");
        String sampleCodeContents = config.get("sampleCodeFileContents");
        String nonexistentContents = config.get("nonexistentFileContents");
        
        System.out.println("assignmentTextFileContents: " + (assignmentContents != null ? "FOUND" : "NOT FOUND"));
        if (assignmentContents != null) {
            System.out.println("Content preview: " + assignmentContents.substring(0, Math.min(50, assignmentContents.length())) + "...");
        }
        
        System.out.println("sampleCodeFileContents: " + (sampleCodeContents != null ? "FOUND" : "NOT FOUND"));
        if (sampleCodeContents != null) {
            System.out.println("Content preview: " + sampleCodeContents.substring(0, Math.min(50, sampleCodeContents.length())) + "...");
        }
        
        System.out.println("nonexistentFileContents: " + (nonexistentContents != null ? "FOUND" : "NOT FOUND"));
        
        // Test 2: Verify user-defined value is not overwritten
        System.out.println("\n=== Test 2: Checking user-defined value preservation ===");
        String userDefinedValue = config.get("assignmentTextFileContents");
        System.out.println("assignmentTextFileContents value: " + userDefinedValue);
        System.out.println("Should be 'user_defined_value': " + "user_defined_value".equals(userDefinedValue));
        
        // Test 3: Verify regular keys still work
        System.out.println("\n=== Test 3: Checking regular keys still work ===");
        String regularKey = config.get("regular_key");
        System.out.println("regular_key: " + regularKey);
        System.out.println("Should be 'regular_value': " + "regular_value".equals(regularKey));
        
        // Test 4: Show full config output
        System.out.println("\n=== Test 4: Full config output ===");
        System.out.println(config.toString());
        
        System.out.println("\n=== Resolved config output ===");
        System.out.println(config.toResolvedString());
        
        System.out.println("\n=== Testing complete! ===");
    }
}