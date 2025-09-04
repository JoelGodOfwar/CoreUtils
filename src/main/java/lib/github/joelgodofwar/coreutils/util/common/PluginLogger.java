package lib.github.joelgodofwar.coreutils.util.common;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLogger {
    private final Logger logger;
    private final String pluginName;
    boolean debug = false;

    public PluginLogger(@NotNull JavaPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null");
        this.logger = plugin.getLogger();
        this.pluginName = plugin.getName();
    }

    public void log(String message) {
        log(message, "");
    }

    public void log(Level level, String message, Object... args) {
        String prefix = ChatColor.RESET + "[" + ChatColor.YELLOW + pluginName + ChatColor.RESET + "] ";
        if (!areAllArgsBlank(args)) {
            Bukkit.getConsoleSender().sendMessage(prefix + String.format(message, args));
        } else {
            Bukkit.getConsoleSender().sendMessage(prefix + message);
        }
    }

    public void log(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void log(Level level, String message, Throwable ex) {
        logger.log(level, message, ex);
    }

    public void debug(String message, boolean debug) {
        this.debug = debug;
        debug(message, "");
    }

    public void debug(String message, Object... args) {
        if (debug) {
            log("[Debug] " + message, args);
        }
    }

    public void debug(String message, Throwable ex) {
        if (debug) {
            log(Level.WARNING, "[Debug] " + message, ex);
        }
    }

    public void warn(String message) {
        warn(message, "");
    }

    public void warn(String message, Object... args) {
        log(Level.WARNING, "[Warning] " + message, args);
    }

    private boolean areAllArgsBlank(Object... args) {
        if ((args == null) || (args.length == 0)) {
            return true;
        }
        for (Object arg : args) {
            if ((arg == null) || ((arg instanceof String) && ((String) arg).isEmpty())) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}