package lib.github.joelgodofwar.coreutils.util;

import lib.github.joelgodofwar.coreutils.util.error.BasicErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.ErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.ReportType;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

public class PluginLibrary {
    private static Plugin plugin;
    private static boolean initialized;
    private static ErrorReporter reporter;

    // Error report types for CoreUtils
    public static final ReportType REPORT_CANNOT_GET_HEXNICK = new ReportType("Cannot get HexNicks nickname");
    public static final ReportType REPORT_ERROR_GETTING_NICKNAME = new ReportType("Error getting nickname");

    public static void init(Plugin plugin, ErrorReporter reporter) {
        Validate.isTrue(!initialized, "CoreUtils has already been initialized.");
        PluginLibrary.plugin = plugin;
        PluginLibrary.reporter = reporter != null ? reporter : new BasicErrorReporter(plugin);
        initialized = true;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static ErrorReporter getErrorReporter() {
        if (!initialized) {
            throw new IllegalStateException("CoreUtils not initialized. Call PluginLibrary.init(plugin, reporter) first.");
        }
        return reporter;
    }
}