package lib.github.joelgodofwar.coreutils.util.error;

import org.bukkit.plugin.Plugin;
import java.util.logging.Level;

public class BasicErrorReporter implements ErrorReporter {
    private final Plugin plugin;

    public BasicErrorReporter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reportDetailed(Object context, Report report) {
        String message = "[" + plugin.getName() + "] Error in " + context.getClass().getSimpleName() + ": " + report.getMessage();
        if (report.getException() != null) {
            message += ", Exception: " + report.getException().getMessage();
            report.getException().printStackTrace();
        }
        plugin.getLogger().log(Level.WARNING, message);
    }
}