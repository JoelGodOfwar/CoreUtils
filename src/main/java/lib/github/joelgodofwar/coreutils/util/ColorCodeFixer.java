package lib.github.joelgodofwar.coreutils.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.github.joelgodofwar.coreutils.version.VersionMatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ColorCodeFixer {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})(?![>:])");
    private static final Pattern ALT_HEX_COLOR_PATTERN = Pattern.compile("§x([A-Fa-f0-9]{6})");
    private static final Pattern ALT_HEX_COLOR_PATTERN_ALT = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
    private static final Pattern NAMED_COLOR_PATTERN = Pattern.compile("<(\\w+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADIENT_OPEN_PATTERN = Pattern.compile("<gradient:[^>]+>");
    private static final Pattern GRADIENT_CLOSE_PATTERN = Pattern.compile("</gradient>");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    /**
     * Fixes color codes for Spigot, outputting legacy § codes or #FFFFFF where supported (Minecraft 1.16+).
     * @param message The input string with color codes.
     * @return The formatted string with § codes (e.g., §x§F§F§F§F§F§F) or #FFFFFF.
     */
    public String fixColorsSpigot(String message) {
        // Step 1: Convert named placeholders to legacy codes
        message = convertNamedPlaceholders(message);

        // Step 2: Convert MiniMessage tags to legacy codes
        message = convertMiniMessageToLegacy(message);

        // Step 3: Convert hex codes to #FFFFFF
        message = convertAltHexFormat(message); // §x§F§F§F§F§F§F to #FFFFFF
        message = convertAltHexFormatSimple(message); // §xFFFFFF to #FFFFFF

        // Step 4: Convert legacy color codes to hex
        message = convertLegacyToHex(message);
        // Step 4.5 Fixes hex with <>
        message = convertStandardHexFormat(message);
        // Step 5: Convert hex to §x§F§F§F§F§F§F (or keep #FFFFFF if supported)
        message = convertHexToBukkit(message);

        // Step 6: Convert legacy formatting codes to § codes
        message = translateAlternateColorCodes(message);

        return message;
    }

    /**
     * Fixes color codes for Paper/Folia, outputting MiniMessage tags.
     * @param message The input string with color codes.
     * @return The formatted string with MiniMessage tags (e.g., <#FFFFFF>, <b>).
     */
    public String fixColorsPaper(String message) {
        // Step 1: Convert named placeholders to legacy codes
        message = convertNamedPlaceholders(message);

        // Step 2: Convert hex codes to #FFFFFF
        message = convertAltHexFormat(message); // §x§F§F§F§F§F§F to #FFFFFF
        message = convertAltHexFormatSimple(message); // §xFFFFFF to #FFFFFF

        // Step 3: Convert legacy codes to hex or MiniMessage
        message = convertLegacyToHex(message);
        message = convertStandardHexFormat(message);
        message = convertLegacyToMiniMessageTag(message);

        // Step 4: Convert #FFFFFF to <#FFFFFF> (gradient-aware)
        message = convertStandardHexFormat(message);

        return message;
    }

    /**
     * Converts named placeholders (e.g., <RED>, <BOLD>) to legacy codes.
     */
    private static String convertNamedPlaceholders(String message) {
        Matcher matcher = NAMED_COLOR_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1).toUpperCase();
            String replacement = switch (placeholder) {
                case "BLACK" -> "&0";
                case "DARK_BLUE" -> "&1";
                case "DARK_GREEN" -> "&2";
                case "DARK_AQUA" -> "&3";
                case "DARK_RED" -> "&4";
                case "DARK_PURPLE" -> "&5";
                case "GOLD" -> "&6";
                case "GRAY" -> "&7";
                case "DARK_GRAY" -> "&8";
                case "BLUE" -> "&9";
                case "GREEN" -> "&a";
                case "AQUA" -> "&b";
                case "RED" -> "&c";
                case "LIGHT_PURPLE" -> "&d";
                case "YELLOW" -> "&e";
                case "WHITE" -> "&f";
                case "MAGIC" -> "&k";
                case "BOLD" -> "&l";
                case "STRIKETHROUGH" -> "&m";
                case "UNDERLINE" -> "&n";
                case "ITALIC" -> "&o";
                case "RESET" -> "&r";
                default -> matcher.group(); // Preserve unknown placeholders
            };
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts MiniMessage tags (e.g., <obf>, <#FFFFFF>) to legacy codes (e.g., §k, §x§F§F§F§F§F§F or #FFFFFF).
     */
    private static String convertMiniMessageToLegacy(String message) {
        // Convert <#FFFFFF> to #FFFFFF (or §x§F§F§F§F§F§F if not supported)
        Matcher matcher = MINI_HEX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        boolean supportsHex = isHexSupported();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            if (supportsHex) {
                matcher.appendReplacement(result, "#" + hexColor);
            } else {
                StringBuilder bukkitCode = new StringBuilder("§x");
                for (char c : hexColor.toCharArray()) {
                    bukkitCode.append("§").append(c);
                }
                matcher.appendReplacement(result, bukkitCode.toString());
            }
        }
        matcher.appendTail(result);

        // Convert MiniMessage formatting tags to legacy
        return result.toString()
                .replace("<obf>", "§k").replace("<b>", "§l").replace("<st>", "§m")
                .replace("<u>", "§n").replace("<i>", "§o").replace("<reset>", "§r");
    }

    /**
     * Converts §x§F§F§F§F§F§F format to #FFFFFF.
     */
    private static String convertAltHexFormat(String message) {
        Matcher matcher = ALT_HEX_COLOR_PATTERN_ALT.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hexColor = matcher.group().replace("§x", "#").replace("§", "");
            matcher.appendReplacement(result, hexColor);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts §xFFFFFF format to #FFFFFF.
     */
    private static String convertAltHexFormatSimple(String message) {
        Matcher matcher = ALT_HEX_COLOR_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hexColor = "#" + matcher.group(1);
            matcher.appendReplacement(result, hexColor);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts legacy codes (&0, §0) to hex codes (#FFFFFF).
     */
    private static String convertLegacyToHex(String message) {
        String prefix = "<";
        String suffix = ">";
        return message.replace("&0", prefix + "#000000" + suffix).replace("&1", prefix + "#0000AA" + suffix).replace("&2", prefix + "#00AA00" + suffix)
                .replace("&3", prefix + "#00AAAA" + suffix).replace("&4", prefix + "#AA0000" + suffix).replace("&5", prefix + "#AA00AA" + suffix)
                .replace("&6", prefix + "#FFAA00" + suffix).replace("&7", prefix + "#AAAAAA" + suffix).replace("&8", prefix + "#555555" + suffix)
                .replace("&9", prefix + "#5555FF" + suffix).replace("&a", prefix + "#55FF55" + suffix).replace("&b", prefix + "#55FFFF" + suffix)
                .replace("&c", prefix + "#FF5555" + suffix).replace("&d", prefix + "#FF55FF" + suffix).replace("&e", prefix + "#FFFF55" + suffix)
                .replace("&f", prefix + "#FFFFFF" + suffix)
                .replace("§0", prefix + "#000000" + suffix).replace("§1", prefix + "#0000AA" + suffix).replace("§2", prefix + "#00AA00" + suffix)
                .replace("§3", prefix + "#00AAAA" + suffix).replace("§4", prefix + "#AA0000" + suffix).replace("§5", prefix + "#AA00AA" + suffix)
                .replace("§6", prefix + "#FFAA00" + suffix).replace("§7", prefix + "#AAAAAA" + suffix).replace("§8", prefix + "#555555" + suffix)
                .replace("§9", prefix + "#5555FF" + suffix).replace("§a", prefix + "#55FF55" + suffix).replace("§b", prefix + "#55FFFF" + suffix)
                .replace("§c", prefix + "#FF5555" + suffix).replace("§d", prefix + "#FF55FF" + suffix).replace("§e", prefix + "#FFFF55" + suffix)
                .replace("§f", prefix + "#FFFFFF" + suffix);
    }

    /**
     * Converts legacy formatting codes (&k, §l) to MiniMessage tags (<obf>, <b>).
     */
    private static String convertLegacyToMiniMessageTag(String message) {
        return message.replace("&k", "<obf>").replace("&l", "<b>").replace("&m", "<st>")
                .replace("&n", "<u>").replace("&o", "<i>").replace("&r", "<reset>")
                .replace("§k", "<obf>").replace("§l", "<b>").replace("§m", "<st>")
                .replace("§n", "<u>").replace("§o", "<i>").replace("§r", "<reset>");
    }

    /**
     * Converts #FFFFFF to <#FFFFFF> unless inside gradients or surrounded by colons.
     */
    private static String convertStandardHexFormat(String message) {
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        int gradientCount = 0;
        Matcher gradientOpenMatcher = GRADIENT_OPEN_PATTERN.matcher(message);
        Matcher gradientCloseMatcher = GRADIENT_CLOSE_PATTERN.matcher(message);

        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            int start = hexMatcher.start();
            int end = hexMatcher.end();

            // Update gradient count
            while (gradientOpenMatcher.find() && gradientOpenMatcher.start() < start) {
                gradientCount++;
            }
            while (gradientCloseMatcher.find() && gradientCloseMatcher.start() < start) {
                gradientCount--;
            }

            boolean insideGradient = gradientCount > 0;
            boolean surroundedByColons = (start > 0 && message.charAt(start - 1) == ':') ||
                    (end < message.length() && message.charAt(end) == ':');

            if (!insideGradient && !surroundedByColons) {
                hexMatcher.appendReplacement(result, "<#" + hexColor + ">");
            } else {
                hexMatcher.appendReplacement(result, "#" + hexColor);
            }
        }
        hexMatcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts #FFFFFF to §x§F§F§F§F§F§F for Spigot (or keeps #FFFFFF if supported).
     */
    private static String convertHexToBukkit(String message) {
        // Check if server supports #FFFFFF (Minecraft 1.16+)
        boolean supportsHex = isHexSupported();
        if (supportsHex) {
            return message; // Keep #FFFFFF
        }

        Pattern pattern = Pattern.compile("#([0-9a-fA-F]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder bukkitCode = new StringBuilder("§x");
            for (char c : hexColor.toCharArray()) {
                bukkitCode.append("§").append(c);
            }
            matcher.appendReplacement(result, bukkitCode.toString());
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Translates & to § for legacy Spigot compatibility.
     */
    public static String translateAlternateColorCodes(String message) {
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    /**
     * Checks if the server supports #FFFFFF hex codes (Minecraft 1.16+).
     */
    private static boolean isHexSupported() {
        return new VersionMatcher(Bukkit.getServer()).isMinecraftVersionAtLeast(1, 16);
    }

    /**
     * Strips all color and formatting codes from a string.
     * @param message The input string with color codes.
     * @return The string with all color/formatting codes removed.
     */
    public static String stripColors(String message) {
        if (message == null) return "";
        // Convert §x§F§F§F§F§F§F to #FFFFFF
        message = convertAltHexFormat(message);
        message = convertAltHexFormatSimple(message);
        // Convert MiniMessage tags to plain text
        message = message.replaceAll("<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|</[A-Z_]+>|\\[<[A-Z_]+>|\\[<#[0-9A-Fa-f]{6}>", "");
        // Convert gradient and rainbow tags
        message = message.replaceAll("<gradient:[^>]+>|</gradient>|<rainbow>|</rainbow>", "");
        // Strip legacy codes
        return ChatColor.stripColor(message);
    }
}