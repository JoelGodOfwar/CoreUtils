/*
 * @author: Aoife (Josh)
 * @date: 2020-04-25
 * @project: WarpConverter
 *
 * Copyright (c) 2020, Aoife (Josh)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.Bukkit;
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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A custom YAML configuration class that preserves comments when loading and saving YAML files.
 * Extends Bukkit's YamlConfiguration for compatibility with Spigot/Bukkit plugins.
 *
 * @author Aoife (Josh)
 */
@SuppressWarnings("deprecation")
public class YmlConfiguration2 extends YamlConfiguration {

    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    private final Map<Integer, String> commentContainer = new HashMap<>();

    /**
     * Saves the configuration to the specified file.
     *
     * @param file The file to save the configuration to.
     * @throws IOException If an I/O error occurs.
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
     *
     * @param file   The file to save the configuration to.
     * @param config The YmlConfiguration instance to save.
     */
    public static void saveConfig(File file, YmlConfiguration2 config) {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save configuration to " + file, e);
        }
    }

    /**
     * Converts the configuration to a YAML string, preserving comments.
     *
     * @return The YAML string representation of the configuration.
     */
    @Override
    public @NotNull String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String header = ""; // buildHeader() is not needed as Bukkit handles headers internally
        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) {
            dump = Strings.EMPTY;
        } else {
            StringBuilder sb = new StringBuilder();
            int line = 0;
            for (String s : dump.split("\n")) {
                line++;
                while (commentContainer.containsKey(line)) {
                    sb.append(commentContainer.get(line)).append("\n");
                    line++;
                }
                sb.append(s).append("\n");
            }
            dump = sb.toString();
        }
        return header.length() > 0 ? header + dump : dump;
    }

    /**
     * Loads the configuration from the specified reader, preserving comments.
     *
     * @param reader The reader to load the configuration from.
     * @throws IOException                   If an I/O error occurs.
     * @throws InvalidConfigurationException If the configuration is invalid.
     */
    @Override
    public void load(@NotNull Reader reader) throws IOException, InvalidConfigurationException {
        Validate.notNull(reader, "Reader cannot be null");
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            int count = 0;
            while ((line = input.readLine()) != null) {
                count++;
                if (line.contains(COMMENT_PREFIX) || line.isEmpty()) {
                    commentContainer.put(count, line);
                }
                builder.append(line).append('\n');
            }
        } finally {
            input.close();
        }
        loadFromString(builder.toString());
    }

    /**
     * Loads a configuration from the specified file.
     *
     * @param file   The file to load the configuration from.
     * @param config The YmlConfiguration instance to load into.
     * @return The loaded configuration.
     */
    public static YmlConfiguration2 loadConfiguration(File file, YmlConfiguration2 config) {
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
}