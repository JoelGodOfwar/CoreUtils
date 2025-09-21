package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.boss.BossBar;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for safe, cached reflection in Bukkit/Spigot plugins.
 * <p>
 * Best practices: Use only when Bukkit API lacks features (e.g., NBT access).
 * Pair with version checks to avoid breaks on MC updates.
 * </p>
 *
 * @author JoelGodOfWar (refactored)
 */
public class ReflectionUtils {

    // Cached fields/methods for performance
    private static Field itemStackHandleField;
    private static Field bossBarHandleField;
    private static Method nbtListGetMethod;

    /**
     * Gets a private field value from an object.
     *
     * @param logger For error logging.
     * @param o The target object.
     * @param c The class containing the field.
     * @param field The field name.
     * @return The field value, or empty if failed.
     */
    public static Optional<Object> getPrivate(Logger logger, Object o, Class<?> c, String field) {
        try {
            Field access = c.getDeclaredField(field);
            access.setAccessible(true);
            return Optional.ofNullable(access.get(o));
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.WARNING, "Failed to get private field " + field + " from " + o.getClass().getName(), ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the NMS handle from a BossBar.
     *
     * @param logger For error logging.
     * @param bossBar The BossBar.
     * @return The handle, or empty if failed.
     */
    public static Optional<Object> getBossBarHandle(Logger logger, BossBar bossBar) {
        try {
            if (bossBarHandleField == null) {
                bossBarHandleField = bossBar.getClass().getDeclaredField("handle");
                bossBarHandleField.setAccessible(true);
            }
            return Optional.ofNullable(bossBarHandleField.get(bossBar));
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Failed to get BossBar handle from " + bossBar.getClass().getName(), ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the NMS handle from an ItemStack.
     *
     * @param logger For error logging.
     * @param itemStack The ItemStack.
     * @param c The ItemStack class (use itemStack.getClass()).
     * @return The handle, or empty if failed.
     */
    public static Optional<Object> getItemStackHandle(Logger logger, ItemStack itemStack, Class<?> c) {
        try {
            if (itemStackHandleField == null) {
                itemStackHandleField = c.getDeclaredField("handle");
                itemStackHandleField.setAccessible(true);
            }
            return Optional.ofNullable(itemStackHandleField.get(itemStack));
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Failed to get ItemStack handle from " + itemStack.getClass().getName(), ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets an item from an NBT list by index.
     *
     * @param logger For error logging.
     * @param listTag The NBT list.
     * @param index The index.
     * @return The item, or empty if failed.
     */
    public static Optional<Object> getNbtListItem(Logger logger, Object listTag, int index) {
        try {
            if (nbtListGetMethod == null) {
                nbtListGetMethod = listTag.getClass().getMethod("get", Integer.TYPE);
            }
            return Optional.ofNullable((Object) nbtListGetMethod.invoke(listTag, index));
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Failed to get NBT list item from " + listTag.getClass().getName(), ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Sets a private field value (with fallback field name for version fixes).
     *
     * @param logger For error logging.
     * @param o The target object.
     * @param c The class.
     * @param field Original field name (logged only).
     * @param fixedField The actual field name to use.
     * @param value The value to set.
     * @return true if successful.
     */
    public static boolean setPrivateWithFallback(Logger logger, Object o, Class<?> c, String field, String fixedField, Object value) {
        if (logger != null && !field.equals(fixedField)) {
            logger.warning("Using fallback field '" + fixedField + "' for '" + field + "' (version fix)");
        }
        return setPrivate(logger, o, c, fixedField, value);
    }

    /**
     * Gets a private field value (with fallback for version fixes).
     *
     * @param logger For error logging.
     * @param o The target object.
     * @param c The class.
     * @param field Original field name (logged only).
     * @param fixedField The actual field name to use.
     * @return The value, or empty if failed.
     */
    public static Optional<Object> getPrivateWithFallback(Logger logger, Object o, Class<?> c, String field, String fixedField) {
        if (logger != null && !field.equals(fixedField)) {
            logger.warning("Using fallback field '" + fixedField + "' for '" + field + "' (version fix)");
        }
        return getPrivate(logger, o, c, fixedField);
    }

    /**
     * Sets a private field value.
     *
     * @param logger For error logging.
     * @param o The target object.
     * @param c The class.
     * @param field The field name.
     * @param value The value to set.
     * @return true if successful.
     */
    public static boolean setPrivate(Logger logger, Object o, Class<?> c, String field, Object value) {
        try {
            Field access = c.getDeclaredField(field);
            access.setAccessible(true);
            access.set(o, value);
            return true;
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Failed to set private field " + field + " on " + o.getClass().getName(), ex);
            }
        }
        return false;
    }

    /**
     * Calls a private method.
     *
     * @param logger For error logging.
     * @param o The target object.
     * @param c The class.
     * @param methodName The method name.
     * @param parameterTypes The parameter types.
     * @param args The arguments.
     * @return true if successful.
     */
    public static boolean callPrivateMethod(Logger logger, Object o, Class<?> c, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = c.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(o, args);
            return true;
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.SEVERE, "Failed to call private method " + methodName + " on " + o.getClass().getName(), ex);
            }
        }
        return false;
    }

    /**
     * Checks if reflection is safe for the current MC version (hook to Version class).
     *
     * @param minVersion Minimum MC version string (e.g., "1.20.2").
     * @return true if current server >= minVersion.
     */
    public static boolean isReflectionSafe(String minVersion) {
        try {
            return Version.getCurrentVersion().isAtLeast(minVersion);
        } catch (Exception e) {
            return false; // Fallback: assume unsafe
        }
    }
}