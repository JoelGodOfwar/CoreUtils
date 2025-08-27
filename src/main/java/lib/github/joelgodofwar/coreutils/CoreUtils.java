package lib.github.joelgodofwar.coreutils;

import lib.github.joelgodofwar.coreutils.util.ColorCodeFixer;
import lib.github.joelgodofwar.coreutils.util.JsonConverter;
import lib.github.joelgodofwar.coreutils.util.JsonMessageUtils;
import lib.github.joelgodofwar.coreutils.util.PluginLibrary;
import lib.github.joelgodofwar.coreutils.util.StrUtils;
import lib.github.joelgodofwar.coreutils.version.VersionMatcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class CoreUtils {
    private final VersionMatcher versionMatcher;
    private final ServerHandler serverHandler;
    private final JavaPlugin plugin;
    public static final ColorCodeFixer ColorCodeFixer = new ColorCodeFixer();
    public static final StrUtils StrUtils = new StrUtils();
    public final ColorCodeFixer colorCodeFixer;
    public final JsonConverter jsonConverter;
    public final JsonMessageUtils jsonMessageUtils;
    public final StrUtils strUtils;

    public CoreUtils(JavaPlugin plugin) {
        PluginLibrary.init(plugin, null);
        this.plugin = plugin;
        this.versionMatcher = new VersionMatcher(plugin.getServer());
        this.serverHandler = versionMatcher.getServerHandler();
        this.colorCodeFixer = new ColorCodeFixer();
        this.jsonConverter = new JsonConverter(plugin);
        this.jsonMessageUtils = new JsonMessageUtils(plugin);
        this.strUtils = new StrUtils();
    }

    public String fixColors(String message) {
        return serverHandler.fixColors(message);
    }

    public void broadcast(String message) {
        String formattedMessage = fixColors(message);
        String jsonMessage = jsonConverter.convert(formattedMessage, "");
        serverHandler.broadcast(this, plugin, jsonMessage);
    }

    public void sendJsonMessage(Player player, String message) {
        String formattedMessage = fixColors(message);
        String jsonMessage = jsonConverter.convert(formattedMessage, "");
        serverHandler.sendJsonMessage(this, plugin, player, jsonMessage);
    }

    public void getNicknameAsync(Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        serverHandler.getNicknameAsync(this, plugin, player, useDisplayName, callback);
    }

    public String getServerTypeName() {
        return versionMatcher.getServerTypeName();
    }

    public VersionMatcher getVersionMatcher() {
        return versionMatcher;
    }
}