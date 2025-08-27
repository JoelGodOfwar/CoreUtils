package lib.github.joelgodofwar.coreutils.util;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts MiniMessage strings to JSON for use with JsonMessageUtils.
 */
public class JsonConverter {

    private final JavaPlugin plugin;
    private final String[] defaultRainbowColors = {"red", "gold", "green", "aqua", "blue", "dark_purple", "light_purple"};
    private final Pattern allTagPattern = Pattern.compile("(<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|§[0-9a-fA-Fk-oK-OrR])|<gradient:#[0-9A-Fa-f]{6}(?::#[0-9A-Fa-f]{6}){1,3}>[^<]+</gradient>|<rainbow>[^<]+</rainbow>");
    ColorCodeFixer colorCodeFixer = CoreUtils.ColorCodeFixer;

    public JsonConverter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Converts a MiniMessage string to a JSON string for TextComponent rendering.
     * @param message The MiniMessage string (e.g., "<#FFFFFF><player> <rainbow>text</rainbow>").
     * @param nickName The player's nickname to replace <player>.
     * @return A JSON string compatible with JsonMessageUtils.
     */
    public String convert(String message, String nickName) {
        // Fix hex codes using colorCodeFixer
        message = colorCodeFixer.fixColorsSpigot(message);
        message = message.replace("<player>", nickName)
                .replace("<colon>", ":")
                .replace("<br>", "\\n")
                .replace("<reset>", "§r");

        StringBuilder json = new StringBuilder("[\"\"");
        String currentColor = "white";
        int lastIndex = 0;
        Matcher matcher = allTagPattern.matcher(message);

        while (matcher.find()) {
            int start = matcher.start();
            if (start > lastIndex) {
                String textPart = message.substring(lastIndex, start).replaceAll("<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|§[0-9a-fA-Fk-oK-OrR>]", "");
                if (!textPart.isEmpty()) {
                    json.append(",{\"text\":\"").append(textPart.replace("\"", "\\\"")).append("\",\"color\":\"").append(currentColor).append("\"}");
                }
            }
            String tag = matcher.group();
            if (tag.matches("<#[0-9A-Fa-f]{6}>")) {
                currentColor = tag.replace("<", "").replace(">", "").toLowerCase();
            } else if (tag.matches("<[A-Z_]+>")) {
                String color = tag.replace("<", "").replace(">", "").toLowerCase();
                if ("reset".equals(color)) {
                    currentColor = "#FFFFFF";
                    lastIndex = matcher.end();
                } else {
                    currentColor = convertToJsonColor(color);
                }
            } else if (tag.matches("§[0-9a-fA-Fk-oK-OrR]")) {
                String code = tag.substring(1).toLowerCase();
                currentColor = switch (code) {
                    case "0" -> "#000000"; // black
                    case "1" -> "#0000AA"; // dark_blue
                    case "2" -> "#00AA00"; // dark_green
                    case "3" -> "#00AAAA"; // dark_aqua
                    case "4" -> "#AA0000"; // dark_red
                    case "5" -> "#AA00AA"; // dark_purple
                    case "6" -> "#FFAA00"; // gold
                    case "7" -> "#AAAAAA"; // gray
                    case "8" -> "#555555"; // dark_gray
                    case "9" -> "#5555FF"; // blue
                    case "a" -> "#55FF55"; // green
                    case "b" -> "#55FFFF"; // aqua
                    case "c" -> "#FF5555"; // red
                    case "d" -> "#FF55FF"; // light_purple
                    case "e" -> "#FFFF55"; // yellow
                    case "f" -> "#FFFFFF"; // white
                    case "r" -> "#FFFFFF"; // reset
                    default -> "white";
                };
            } else if (tag.matches("<gradient:#[0-9A-Fa-f]{6}(?::#[0-9A-Fa-f]{6}){1,3}>[^<]+</gradient>")) {
                String[] parts = tag.replace("</gradient>", "").split(">");
                String colorString = parts[0].replace("<gradient:", "");
                String text = parts[1].replaceAll("<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|§[0-9a-fA-Fk-oK-OrR>]", "");
                List<String> colors = new ArrayList<>();
                for (String color : colorString.split(":")) {
                    if (color.matches("#[0-9A-Fa-f]{6}")) {
                        colors.add(color.toLowerCase());
                    }
                }
                if (colors.size() >= 2 && colors.size() <= 4) {
                    json.append(",").append(generateGradientJson(text, colors));
                } else {
                    plugin.getLogger().warning("Invalid gradient tag (must have 2-4 valid hex colors): " + tag);
                }
            } else if (tag.matches("<rainbow>[^<]+</rainbow>")) {
                String text = tag.replace("<rainbow>", "").replace("</rainbow>", "").replaceAll("<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|§[0-9a-fA-Fk-oK-OrR>]", "");
                json.append(",").append(generateRainbowJson(text));
            }
            if (!tag.matches("<[A-Z_]+>")) {
                lastIndex = matcher.end();
            }
        }

        if (lastIndex < message.length()) {
            String textPart = message.substring(lastIndex).replaceAll("<#[0-9A-Fa-f]{6}>|<[A-Z_]+>|§[0-9a-fA-Fk-oK-OrR>]", "");
            if (!textPart.isEmpty()) {
                json.append(",{\"text\":\"").append(textPart.replace("\"", "\\\"")).append("\",\"color\":\"").append(currentColor).append("\"}");
            }
        }

        json.append("]");
        return json.toString();
    }

    /**
     * Converts a MiniMessage color name or hex code to a JSON-compatible color.
     * @param color The color name (e.g., "red", "reset") or hex code (e.g., "#FF5555").
     * @return A JSON-compatible color (e.g., "#FF5555", "red").
     */
    public String convertToJsonColor(String color) {
        color = color.toLowerCase();
        return switch (color) {
            case "black" -> "#000000";
            case "dark_blue" -> "#0000AA";
            case "dark_green" -> "#00AA00";
            case "dark_aqua" -> "#00AAAA";
            case "dark_red" -> "#AA0000";
            case "dark_purple" -> "#AA00AA";
            case "gold" -> "#FFAA00";
            case "gray" -> "#AAAAAA";
            case "dark_gray" -> "#555555";
            case "blue" -> "#5555FF";
            case "green" -> "#55FF55";
            case "aqua" -> "#55FFFF";
            case "red" -> "#FF5555";
            case "light_purple" -> "#FF55FF";
            case "yellow" -> "#FFFF55";
            case "white" -> "#FFFFFF";
            case "reset" -> "#FFFFFF";
            default -> color.matches("#[0-9A-Fa-f]{6}") ? color : "white";
        };
    }

    private String generateGradientJson(String text, List<String> colors) {
        if (text.isEmpty() || colors.size() < 2 || colors.size() > 4) return "";
        StringBuilder json = new StringBuilder();
        int length = text.length();
        int segments = colors.size() - 1;
        double charsPerSegment = (double) length / segments;

        for (int i = 0; i < length; i++) {
            int segment = Math.min((int) (i / charsPerSegment), segments - 1);
            double segmentRatio = (i - segment * charsPerSegment) / charsPerSegment;
            if (i >= length - 1 && segment == segments - 1) {
                segmentRatio = 1.0;
            }

            int[] startRGB = hexToRGB(colors.get(segment));
            int[] endRGB = hexToRGB(colors.get(segment + 1));
            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * segmentRatio);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * segmentRatio);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * segmentRatio);
            String hex = String.format("#%02X%02X%02X", r, g, b);

            json.append("{\"text\":\"").append(text.charAt(i)).append("\",\"color\":\"").append(hex).append("\"}");
            if (i < length - 1) json.append(",");
        }
        return json.toString();
    }

    private int[] hexToRGB(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private String generateRainbowJson(String text) {
        if (text.isEmpty()) return "";
        String[] colors = getRainbowColors();
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String color = colors[i % colors.length];
            json.append("{\"text\":\"").append(text.charAt(i)).append("\",\"color\":\"").append(color).append("\"}");
            if (i < text.length() - 1) json.append(",");
        }
        return json.toString();
    }

    private String[] getRainbowColors() {
        List<String> colors = plugin.getConfig().getStringList("rainbow_colors");
        return colors.isEmpty() ? defaultRainbowColors : colors.toArray(new String[0]);
    }
}