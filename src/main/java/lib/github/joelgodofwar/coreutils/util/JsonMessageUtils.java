package lib.github.joelgodofwar.coreutils.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface JsonMessageUtils {
    /**
     * Sends a message to a player, using platform-native formatting.
     * Assumes placeholders replaced upstream.
     */
    void sendMessage(Player player, String message);

    /**
     * Sends a pre-processed JSON string to a player.
     */
    void sendJsonMessage(Player player, String json);

    /**
     * Deserializes JSON to a platform-native component (for internal use).
     */
    Object componentFromJson(String json);  // Object to avoid type coupling; cast in impls
}