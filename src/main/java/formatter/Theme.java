package formatter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable theme definition loaded from a JSON file:
 * {
 *   "name": "...",
 *   "description": "...",
 *   "background": "#xxxxxx",
 *   "foreground": "#xxxxxx",
 *   "styles": { "keyword": "color:#..;", ... }
 * }
 */
public final class Theme {

    private final String name;
    private final String description;
    private final String background;
    private final String foreground;
    private final Map<String,String> styles;

    public Theme(String name,
                 String description,
                 String background,
                 String foreground,
                 Map<String,String> styles) {
        this.name = name;
        this.description = description;
        this.background = background;
        this.foreground = foreground;
        this.styles = Collections.unmodifiableMap(new LinkedHashMap<>(styles));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBackground() {
        return background;
    }

    public String getForeground() {
        return foreground;
    }

    public Map<String,String> getStyles() {
        return styles;
    }

    /**
     * Returns style string for token; if absent returns a fallback using foreground color.
     */
    public String styleFor(String token) {
        String s = styles.get(token);
        if (s == null || s.isBlank()) {
            return "color:" + (foreground == null ? "#000000" : foreground) + ";";
        }
        // Ensure there's at least a color directive
        if (!s.contains("color:") && foreground != null) {
            return "color:" + foreground + ";" + s;
        }
        return s;
    }

    @Override
    public String toString() {
        return "Theme{name='" + name + "', styles=" + styles.size() + "}";
    }
}