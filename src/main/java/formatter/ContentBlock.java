// Current filename: ContentBlock.java

package formatter;

import java.util.Map;

/**
 * Represents a block of content in the discussion post.
 * 
 * ContentBlocks are the building blocks for assembling the final HTML output.
 * Each block has a type that determines how it should be rendered and metadata
 * that provides additional context for processing.
 * 
 * TODO: Consider converting to a record when upgrading to Java 14+
 * TODO: Add validation for content and metadata
 * TODO: Implement content transformation methods
 */
public interface ContentBlock {
    
    /**
     * The type of content this block represents.
     */
    enum Type {
        ASSIGNMENT_TEXT,
        HIGHLIGHTED_CODE,
        PROGRAM_OUTPUT,
        EXPLANATION_PLACEHOLDER,
        INLINE_CODE,
        SECTION_HEADER
    }
    
    /**
     * @return The type of this content block
     */
    Type getType();
    
    /**
     * @return The raw content string for this block
     */
    String getContent();
    
    /**
     * @return Optional metadata associated with this block
     */
    Map<String, String> getMetadata();
    
    /**
     * Default implementation of ContentBlock for simple use cases.
     * 
     * TODO: Add builder pattern for complex content blocks
     * TODO: Implement content validation in constructor
     */
    class DefaultContentBlock implements ContentBlock {
        private final Type type;
        private final String content;
        private final Map<String, String> metadata;
        
        public DefaultContentBlock(Type type, String content, Map<String, String> metadata) {
            this.type = type;
            this.content = content != null ? content : "";
            this.metadata = metadata != null ? metadata : Map.of();
        }
        
        public DefaultContentBlock(Type type, String content) {
            this(type, content, null);
        }
        
        @Override
        public Type getType() {
            return type;
        }
        
        @Override
        public String getContent() {
            return content;
        }
        
        @Override
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}