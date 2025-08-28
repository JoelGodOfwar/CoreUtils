package lib.github.joelgodofwar.coreutils.util;

public final class Validate {
    private Validate() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates that the given condition is true.
     *
     * @param expression the boolean expression to check
     * @param message the exception message if the condition is false
     * @throws IllegalStateException if the expression is false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Validates that the given object is not null.
     *
     * @param object the object to check
     * @param message the exception message if the object is null
     * @throws IllegalArgumentException if the object is null
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}