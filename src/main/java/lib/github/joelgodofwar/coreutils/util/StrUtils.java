package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class StrUtils {
    private StrUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the rightmost characters of a string up to the specified length.
     *
     * @param input The input string.
     * @param chars The number of characters to retrieve from the right.
     * @return The rightmost characters, or the full string if shorter.
     * @throws IllegalArgumentException if chars is negative.
     */
    public static String Right(@Nullable String input, int chars) {
        Validate.isTrue(chars >= 0, "Number of characters must be non-negative");
        if (Strings.isNullOrEmpty(input) || input.length() <= chars) {
            return input;
        }
        return input.substring(input.length() - chars);
    }

    /**
     * Returns the leftmost characters of a string up to the specified length.
     *
     * @param input The input string.
     * @param chars The number of characters to retrieve from the left.
     * @return The leftmost characters, or the full string if shorter.
     * @throws IllegalArgumentException if chars is negative.
     */
    public static String Left(@Nullable String input, int chars) {
        Validate.isTrue(chars >= 0, "Number of characters must be non-negative");
        if (Strings.isNullOrEmpty(input) || input.length() <= chars) {
            return input;
        }
        return input.substring(0, chars);
    }

    /**
     * Checks if a string contains a specified substring, ignoring case.
     *
     * @param string  The input string (comma-separated values).
     * @param string2 The substring to check for.
     * @return True if the substring is found (case-insensitive).
     */
    public static boolean stringContains(@Nullable String string, @Nullable String string2) {
        if (Strings.isNullOrEmpty(string) || Strings.isNullOrEmpty(string2)) {
            return false;
        }
        String[] parts = string.toUpperCase().split(", ");
        for (String part : parts) {
            if (part.equals(string2.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares two strings for equality after removing color codes.
     *
     * @param string1 The first string.
     * @param string2 The second string.
     * @return True if the strings are equal (ignoring color codes).
     */
    public static boolean stringEquals(@Nullable String string1, @Nullable String string2) {
        if (string1 == null || string2 == null) {
            return Objects.equals(string1, string2);
        }
        return ChatColor.stripColor(string1).equals(ChatColor.stripColor(string2));
    }

    /**
     * Converts the first character of a string to uppercase, rest to lowercase.
     *
     * @param input The input string.
     * @return The string in Title Case, or original if null/empty.
     */
    public static String toTitleCase(@Nullable String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Converts each word in a string to title case.
     *
     * @param input The input string.
     * @return The string with each word capitalized, or original if null/empty.
     */
    public static String toProperTitleCase(@Nullable String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!Strings.isNullOrEmpty(words[i])) {
                result.append(words[i].substring(0, 1).toUpperCase())
                        .append(words[i].substring(1).toLowerCase());
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Removes blank lines from a list of strings.
     *
     * @param lore The list of strings (e.g., item lore).
     * @return A new list with non-blank strings.
     */
    public static @NotNull List<String> removeBlanks(@Nullable List<String> lore) {
        List<String> cleanedLore = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                if (!Strings.isNullOrEmpty(line)) {
                    cleanedLore.add(line);
                }
            }
        }
        return cleanedLore;
    }
}