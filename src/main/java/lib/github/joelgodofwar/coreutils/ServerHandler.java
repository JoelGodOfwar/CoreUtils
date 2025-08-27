package lib.github.joelgodofwar.coreutils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public interface ServerHandler {
    String fixColors(String message);
    void broadcast(CoreUtils coreUtils, Plugin plugin, String jsonMessage);
    void sendJsonMessage(CoreUtils coreUtils, Plugin plugin, Player player, String jsonMessage);
    void getNicknameAsync(CoreUtils coreUtils, Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback);
}