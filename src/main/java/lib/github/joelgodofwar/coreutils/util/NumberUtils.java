package lib.github.joelgodofwar.coreutils.util;

import org.jetbrains.annotations.Nullable;

public final class NumberUtils {
    private NumberUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a String to an int, throwing an exception if the conversion fails.
     *
     * @param str The string to convert.
     * @return The int represented by the string.
     * @throws NumberFormatException If the string is null, empty, or not a valid integer.
     */
    public static int toInt(@Nullable String str) {
        Validate.notNull(str, "String cannot be null");
        if (Strings.isNullOrEmpty(str)) {
            throw new NumberFormatException("Empty string is not a valid integer");
        }
        return Integer.parseInt(str);
    }

    /**
     * Converts a String to an int, returning a default value if the conversion fails.
     *
     * @param str          The string to convert.
     * @param defaultValue The default value to return if conversion fails.
     * @return The int represented by the string, or the default value if conversion fails.
     */
    public static int toInt(@Nullable String str, int defaultValue) {
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Converts a String to a long, throwing an exception if the conversion fails.
     *
     * @param str The string to convert.
     * @return The long represented by the string.
     * @throws NumberFormatException If the string is null, empty, or not a valid long.
     */
    public static long toLong(@Nullable String str) {
        Validate.notNull(str, "String cannot be null");
        if (Strings.isNullOrEmpty(str)) {
            throw new NumberFormatException("Empty string is not a valid long");
        }
        return Long.parseLong(str);
    }

    /**
     * Converts a String to a long, returning a default value if the conversion fails.
     *
     * @param str          The string to convert.
     * @param defaultValue The default value to return if conversion fails.
     * @return The long represented by the string, or the default value if conversion fails.
     */
    public static long toLong(@Nullable String str, long defaultValue) {
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Converts a String to a float, throwing an exception if the conversion fails.
     *
     * @param str The string to convert.
     * @return The float represented by the string.
     * @throws NumberFormatException If the string is null, empty, or not a valid float.
     */
    public static float toFloat(@Nullable String str) {
        Validate.notNull(str, "String cannot be null");
        if (Strings.isNullOrEmpty(str)) {
            throw new NumberFormatException("Empty string is not a valid float");
        }
        return Float.parseFloat(str);
    }

    /**
     * Converts a String to a float, returning a default value if the conversion fails.
     *
     * @param str          The string to convert.
     * @param defaultValue The default value to return if conversion fails.
     * @return The float represented by the string, or the default value if conversion fails.
     */
    public static float toFloat(@Nullable String str, float defaultValue) {
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Converts a String to a double, throwing an exception if the conversion fails.
     *
     * @param str The string to convert.
     * @return The double represented by the string.
     * @throws NumberFormatException If the string is null, empty, or not a valid double.
     */
    public static double toDouble(@Nullable String str) {
        Validate.notNull(str, "String cannot be null");
        if (Strings.isNullOrEmpty(str)) {
            throw new NumberFormatException("Empty string is not a valid double");
        }
        return Double.parseDouble(str);
    }

    /**
     * Converts a String to a double, returning a default value if the conversion fails.
     *
     * @param str          The string to convert.
     * @param defaultValue The default value to return if conversion fails.
     * @return The double represented by the string, or the default value if conversion fails.
     */
    public static double toDouble(@Nullable String str, double defaultValue) {
        if (Strings.isNullOrEmpty(str)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Checks whether the String contains only Unicode digits.
     *
     * @param str The string to check.
     * @return {@code true} if the string contains only digits, {@code false} otherwise.
     */
    public static boolean isDigits(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}