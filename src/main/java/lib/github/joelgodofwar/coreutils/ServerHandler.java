package lib.github.joelgodofwar.coreutils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public interface ServerHandler {
    void getNicknameAsync(Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback);
}