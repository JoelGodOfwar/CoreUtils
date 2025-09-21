package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A custom YAML configuration class that preserves the structure of the default config.yml from the JAR,
 * including comments and formatting, while using user values or defaults for missing keys.
 * Extends Bukkit's YamlConfiguration for compatibility with Spigot/Bukkit plugins.
 *
 * @author Aoife (Josh)
 */
@SuppressWarnings("deprecation")
public class YmlConfiguration extends YamlConfiguration {

    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    private final List<String> defaultLines = new ArrayList<>();
    private final Map<String, Integer> keyLineMap = new HashMap<>();
    private final Map<String, Integer> indentationMap = new HashMap<>();
    private final Map<String, Object> defaultValues = new HashMap<>();
    private final Plugin plugin;

    /**
     * Constructor to initialize with the plugin instance for accessing JAR resources.
     */
    public YmlConfiguration(Plugin plugin) {
        this.plugin = plugin;
        loadDefaultConfig();
    }

    /**
     * Loads the default config.yml from the JAR and stores its lines and values.
     */
    private void loadDefaultConfig() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("config.yml"), Charsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String currentPath = "";
            String line;
            int lineNumber = -1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                defaultLines.add(line);
                builder.append(line).append('\n');
                String trimmedLine = line.trim();
                if (trimmedLine.contains(":") && !trimmedLine.startsWith("-")) {
                    String key = trimmedLine.split(":")[0].trim();
                    currentPath = updatePath(currentPath, line);
                    String fullPath = currentPath.isEmpty() ? key : currentPath + "." + key;
                    keyLineMap.put(fullPath, lineNumber);
                    indentationMap.put(fullPath, line.length() - trimmedLine.length());
                }
            }
            // Load default values into a separate configuration
            YamlConfiguration defaultConfig = new YamlConfiguration();
            defaultConfig.loadFromString(builder.toString());
            defaultValues.putAll(defaultConfig.getValues(false));
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load default config.yml from JAR", e);
        }
    }

    /**
     * Saves the configuration to the specified file.
     */
    @Override
    public void save(@NotNull File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(saveToString());
        }
    }

    /**
     * Saves the given configuration to the specified file.
     */
    public static void saveConfig(File file, YmlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save configuration to " + file, e);
        }
    }

    /**
     * Converts the configuration to a YAML string, using the default config's structure.
     */
    @Override
    public @NotNull String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        StringBuilder sb = new StringBuilder();
        Map<String, Object> currentValues = getValues(false);

        for (int i = 0; i < defaultLines.size(); i++) {
            String line = defaultLines.get(i);
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
                sb.append(line).append("\n");
            } else if (trimmedLine.contains(":") && !trimmedLine.startsWith("-")) {
                String key = trimmedLine.split(":")[0].trim();
                String currentPath = getPathForLine(i);
                Object value = currentValues.getOrDefault(currentPath, defaultValues.get(currentPath));
                if (value != null) {
                    String indent = line.substring(0, line.length() - trimmedLine.length());
                    String formattedValue = formatValue(value);
                    sb.append(indent).append(key).append(": ").append(formattedValue).append("\n");
                } else {
                    // Skip lines for keys that are not in current or default values
                    continue;
                }
            } else {
                sb.append(line).append("\n");
            }
        }

        // Append any new keys not present in the default config
        for (Map.Entry<String, Object> entry : currentValues.entrySet()) {
            if (!keyLineMap.containsKey(entry.getKey())) {
                sb.append(formatNewKey(entry.getKey(), entry.getValue())).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Loads the configuration from the specified reader, preserving user values.
     */
    @Override
    public void load(@NotNull Reader reader) throws IOException, InvalidConfigurationException {
        Validate.notNull(reader, "Reader cannot be null");
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            input.close();
        }
        loadFromString(builder.toString());
    }

    /**
     * Updates the current path based on indentation and line content.
     */
    private String updatePath(String currentPath, String line) {
        int indentLevel = line.length() - line.trim().length();
        if (indentLevel == 0) return "";
        String[] pathParts = currentPath.isEmpty() ? new String[0] : currentPath.split("\\.");
        int expectedParts = indentLevel / options().indent();
        StringBuilder newPath = new StringBuilder();
        for (int i = 0; i < expectedParts && i < pathParts.length; i++) {
            newPath.append(pathParts[i]).append(".");
        }
        return newPath.length() > 0 ? newPath.substring(0, newPath.length() - 1) : "";
    }

    /**
     * Gets the configuration path for a given line number.
     */
    private String getPathForLine(int lineNumber) {
        for (Map.Entry<String, Integer> entry : keyLineMap.entrySet()) {
            if (entry.getValue() == lineNumber) {
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     * Formats a configuration value for YAML output.
     */
    private String formatValue(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                sb.append("\n  - ").append(item.toString());
            }
            return sb.toString();
        }
        return value.toString();
    }

    /**
     * Formats a new key for appending to the configuration.
     */
    private String formatNewKey(String path, Object value) {
        String[] parts = path.split("\\.");
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            indent.append("  ");
        }
        String key = parts[parts.length - 1];
        if (value instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append(key).append(":");
            for (Object item : (List<?>) value) {
                sb.append("\n").append(indent).append("  - ").append(item.toString());
            }
            return sb.toString();
        }
        return indent + key + ": " + value.toString();
    }

    /**
     * Loads a configuration from the specified file.
     */
    public static YmlConfiguration loadConfiguration(File file, YmlConfiguration config) {
        Validate.notNull(file, "File cannot be null");
        try {
            config.load(file);
        } catch (FileNotFoundException ex) {
            // Silently ignore if file doesn't exist
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
        return config;
    }

    // In lib.github.joelgodofwar.coreutils.util.YmlConfiguration (add this method)
    public Double getSafeDouble(String path, Double def) {
        String str = getString(path);
        if (str == null || str.trim().isEmpty()) {
            return (def != null) ? def : 0.0;
        }
        // Strip surrounding quotes/apostrophes (handles your old issue)
        str = stripQuotes(str.trim());
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            if (plugin.getLogger() != null) {
                plugin.getLogger().warning("Invalid double at '" + path + "': " + str + " (using default)");
            }
            return (def != null) ? def : 0.0;
        }
    }

    private static String stripQuotes(String str) {
        if (str.length() >= 2) {
            char first = str.charAt(0);
            char last = str.charAt(str.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return str.substring(1, str.length() - 1).trim();
            }
        }
        return str;
    }

}