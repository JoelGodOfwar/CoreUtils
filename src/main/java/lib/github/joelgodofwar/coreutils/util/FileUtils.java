package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static void copyFile(String origin, String destination) throws IOException {
        Path from = Paths.get(origin);
        Path to = Paths.get(destination);
        // Ensure the destination directory exists
        Files.createDirectories(to.getParent());
        // Overwrite the destination file if it exists and copy file attributes
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public static void backupFile(JavaPlugin plugin, String origin) throws IOException {
        Path from = Paths.get(origin);
        Path to = plugin.getDataFolder().toPath()
                .resolve("backup")
                .resolve(from.getFileName() + ".bak");
        copyFile(origin, to.toString());
    }

    public static void backupFileWithTimestamp(JavaPlugin plugin, String origin) throws IOException {
        Path from = Paths.get(origin);
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(LocalDateTime.now());
        Path to = plugin.getDataFolder().toPath()
                .resolve("backup")
                .resolve(from.getFileName() + "_" + timestamp + ".bak");
        copyFile(origin, to.toString());
    }

    public static void renameFile(String oldPath, String newPath) throws IOException {
        Path from = Paths.get(oldPath);
        Path to = Paths.get(newPath);
        Files.createDirectories(to.getParent());
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void moveFile(String origin, String destination) throws IOException {
        Path from = Paths.get(origin);
        Path to = Paths.get(destination);
        Files.createDirectories(to.getParent());
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    public static List<Path> listFiles(JavaPlugin plugin, String dirName, String extension) throws IOException {
        Path dirPath = plugin.getDataFolder().toPath().resolve(dirName);
        try (Stream<Path> stream = Files.list(dirPath)) {
            return stream
                    .filter(path -> Files.isRegularFile(path) && (extension == null || path.toString().endsWith(extension)))
                    .collect(Collectors.toList());
        }
    }

    public static void pruneBackups(JavaPlugin plugin, int maxBackups) {
        List<Path> backups;
        try {
            backups = listFiles(plugin, "backup", ".bak");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to list backups: " + e.getMessage());
            return; // Exit gracefully if directory access fails
        }
        backups.stream()
                .sorted((p1, p2) -> {
                    try {
                        return Long.compare(Files.getLastModifiedTime(p2).toMillis(), Files.getLastModifiedTime(p1).toMillis());
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to compare backup times: " + p1 + " vs " + p2);
                        return 0; // Fallback: treat as equal
                    }
                })
                .skip(maxBackups)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to delete backup: " + path);
                    }
                });
    }

    // New asynchronous method with default maxBackups
    public static void pruneBackupsAsync(JavaPlugin plugin) {
        pruneBackupsAsync(plugin, 10); // Default to keeping 10 backups
    }

    public static void pruneBackupsAsync(JavaPlugin plugin, int maxBackups) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                pruneBackups(plugin, maxBackups);
            } catch (Exception e) {
                plugin.getLogger().warning("Error pruning backups asynchronously: " + e.getMessage());
            }
        });
    }

}