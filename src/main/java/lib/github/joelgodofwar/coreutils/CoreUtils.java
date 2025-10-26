package lib.github.joelgodofwar.coreutils;

import lib.github.joelgodofwar.coreutils.util.*;
import lib.github.joelgodofwar.coreutils.util.common.PluginLibrary;
import lib.github.joelgodofwar.coreutils.version.VersionMatcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CoreUtils {
    public static boolean debug = false;
    private final VersionMatcher versionMatcher;
    private final ServerHandler serverHandler;
    private final JavaPlugin plugin;
    public static final ColorCodeFixer ColorCodeFixer = new ColorCodeFixer();
    public final ColorCodeFixer colorCodeFixer;
    public final JsonConverter jsonConverter;
    public final JsonMessageUtils jsonMessageUtils;

    public CoreUtils(JavaPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null");
        PluginLibrary.init(plugin, null);
        this.plugin = plugin;
        this.versionMatcher = new VersionMatcher(plugin.getServer());
        this.serverHandler = versionMatcher.getServerHandler();
        this.colorCodeFixer = new ColorCodeFixer();
        this.jsonConverter = new JsonConverter(plugin);
        this.jsonMessageUtils = versionMatcher.createJsonMessageUtils(plugin, this);
        plugin.getLogger().info("CoreUtils: ServerType=" + versionMatcher.getServerTypeName() +
                " | Using JsonMessageUtils=" + jsonMessageUtils.getClass().getSimpleName());
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

    public String LoadTime(long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
        long milliseconds = elapsedTime % 1000;

        if (minutes > 0) {
            return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
        } else if (seconds > 0) {
            return String.format("%d s %d ms.", seconds, milliseconds);
        } else {
            return String.format("%d ms.", elapsedTime);
        }
    }
}