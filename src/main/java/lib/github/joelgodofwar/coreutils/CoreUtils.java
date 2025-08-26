package lib.github.joelgodofwar.coreutils;

import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import lib.github.joelgodofwar.coreutils.util.PluginLibrary;
import lib.github.joelgodofwar.coreutils.version.VersionMatcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;

public class CoreUtils {
    private final VersionMatcher versionMatcher;
    private final ServerHandler serverHandler;

    // Expose ChatColorUtils as a static field
    public static final ChatColorUtils ChatColorUtils = new ChatColorUtils();

    public CoreUtils(Plugin plugin) {
        PluginLibrary.init(plugin, null); // Use default BasicErrorReporter
        this.versionMatcher = new VersionMatcher(plugin.getServer());
        this.serverHandler = versionMatcher.getServerHandler();
    }

    public void getNicknameAsync(Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        serverHandler.getNicknameAsync(plugin, player, useDisplayName, callback);
    }

    public String getServerTypeName() {
        return versionMatcher.getServerTypeName();
    }
}