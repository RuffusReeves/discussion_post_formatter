package formatter;

/**
 * Test key transformation logic.
 */
public class KeyTransformTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing key transformation logic...\n");
        
        // Create a simple config to access the transformation method via reflection
        Config config = Config.load("test_config.txt");
        
        // Test various key transformations by checking the actual results
        String[] testKeys = {
            "assignment_text_file_address",
            "assignment_sample_code_file_address", 
            "code_file_address",
            "output_file_address",
            "simple_address",
            "file_address",
            "address_only"
        };
        
        String[] expectedResults = {
            "assignmentTextFileContents",
            "assignmentSampleCodeFileContents",
            "codeFileContents", 
            "outputFileContents",
            "simpleContents",
            "fileContents",
            "contents"
        };
        
        // Use reflection to test the private method
        java.lang.reflect.Method generateDerivedKey = Config.class.getDeclaredMethod("generateDerivedKey", String.class);
        generateDerivedKey.setAccessible(true);
        
        System.out.println("Key transformation tests:");
        for (int i = 0; i < testKeys.length; i++) {
            String result = (String) generateDerivedKey.invoke(config, testKeys[i]);
            boolean matches = expectedResults[i].equals(result);
            System.out.printf("%-40s -> %-30s %s\n", 
                testKeys[i], result, matches ? "✓" : "✗ (expected: " + expectedResults[i] + ")");
        }
        
        System.out.println("\nKey transformation test complete!");
    }
}