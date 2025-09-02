package formatter;

/**
 * Test that derived values are not persisted when saving config.
 */
public class PersistenceTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that derived values are not persisted...\n");
        
        // Load test config with existing files
        Config config = Config.load("test_config.txt");
        
        // Verify we have derived values
        String sampleCodeContents = config.get("sampleCodeFileContents");
        System.out.println("sampleCodeFileContents exists: " + (sampleCodeContents != null));
        
        // Save to a new file and reload
        config.save(java.nio.file.Path.of("test_config_saved.txt"));
        
        // Load the saved file and check if derived values were persisted
        Config reloaded = Config.load("test_config_saved.txt");
        String reloadedSampleCode = reloaded.get("sampleCodeFileContents");
        
        System.out.println("After save/reload, sampleCodeFileContents exists: " + (reloadedSampleCode != null));
        System.out.println("Should be false (not persisted): " + (reloadedSampleCode == null));
        
        // Show the saved file content
        System.out.println("\nSaved file content:");
        String savedContent = formatter.Utils.readFile("test_config_saved.txt");
        System.out.println(savedContent);
        
        System.out.println("\nPersistence test complete!");
    }
}